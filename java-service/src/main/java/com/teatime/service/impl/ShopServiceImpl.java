package com.teatime.service.impl;

import static com.teatime.utils.RedisConstants.CACHE_SHOP_KEY;
import static com.teatime.utils.RedisConstants.CACHE_SHOP_TTL;
import static com.teatime.utils.RedisConstants.SHOP_GEO_KEY;

import com.teatime.dto.Result;
import com.teatime.entity.Shop;
import com.teatime.repository.ShopRepository;
import com.teatime.service.IShopService;
import com.teatime.utils.CacheClient;
import com.teatime.utils.RedisFallback;
import com.teatime.utils.SystemConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Service implementation for shop management
 * </p>
 */
@Service
public class ShopServiceImpl implements IShopService {

  @Resource
  private ShopRepository shopRepository;

  @Resource
  private StringRedisTemplate stringRedisTemplate;

  @Resource
  private CacheClient cacheClient;

  @Override
  public Result queryById(Long id) {
    Shop shop = cacheClient.queryWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class, this::getById,
        CACHE_SHOP_TTL, TimeUnit.MINUTES);
    if (shop == null) {
      return Result.fail("Shop does not exist!");
    }
    return Result.ok(shop);
  }

  @Override
  @Transactional
  public Result update(Shop shop) {
    Long id = shop.getId();
    if (id == null) {
      return Result.fail("Shop id cannot be null!");
    }
    shopRepository.save(shop);
    RedisFallback.executeVoid(() ->
        stringRedisTemplate.delete(CACHE_SHOP_KEY + shop.getId()));
    return Result.ok();
  }

  @Override
  public Result queryShopByType(Integer typeId, Integer current, Double x, Double y) {
    if (x == null || y == null) {
      List<Shop> shops = shopRepository.findByTypeId(
          typeId.longValue(),
          PageRequest.of(current - 1, SystemConstants.DEFAULT_PAGE_SIZE)
      ).getContent();
      return Result.ok(shops);
    }

    int from = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
    int end = current * SystemConstants.DEFAULT_PAGE_SIZE;

    GeoResults<RedisGeoCommands.GeoLocation<String>> results = RedisFallback.execute(
        () -> {
          String key = SHOP_GEO_KEY + typeId;
          return stringRedisTemplate.opsForGeo()
              .search(key, GeoReference.fromCoordinate(x, y), new Distance(5000),
                  RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs()
                      .includeDistance().limit(end));
        },
        () -> null
    );

    if (results == null) {
      // Redis unavailable or no geo data - fallback to DB query by type (no distance ordering)
      List<Shop> shops = shopRepository.findByTypeId(
          typeId.longValue(),
          PageRequest.of(current - 1, SystemConstants.DEFAULT_PAGE_SIZE)
      ).getContent();
      return Result.ok(shops);
    }

    List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
    if (list == null || list.size() <= from) {
      return Result.ok(Collections.emptyList());
    }

    List<Long> ids = new ArrayList<>(list.size());
    Map<String, Distance> distanceMap = new HashMap<>(list.size());
    list.stream().skip(from).forEach(geoResult -> {
      String shopIdStr = geoResult.getContent().getName();
      ids.add(Long.valueOf(shopIdStr));
      distanceMap.put(shopIdStr, geoResult.getDistance());
    });

    List<Shop> shops = shopRepository.findByIdIn(ids);
    Map<Long, Integer> orderMap = new HashMap<>();
    for (int i = 0; i < ids.size(); i++) {
      orderMap.put(ids.get(i), i);
    }
    shops.sort(Comparator.comparingInt(s -> orderMap.getOrDefault(s.getId(), Integer.MAX_VALUE)));

    for (Shop shop : shops) {
      Distance dist = distanceMap.get(shop.getId().toString());
      if (dist != null) {
        shop.setDistance(dist.getValue());
      }
    }

    return Result.ok(shops);
  }

  @Override
  public Result queryShopByName(String name, Integer current) {
    List<Shop> shops;
    if (StringUtils.isNotBlank(name)) {
      shops = shopRepository.findByNameContaining(
          name,
          PageRequest.of(current - 1, SystemConstants.MAX_PAGE_SIZE)
      ).getContent();
    } else {
      shops = shopRepository.findAll(PageRequest.of(current - 1, SystemConstants.MAX_PAGE_SIZE))
          .getContent();
    }
    return Result.ok(shops);
  }

  @Override
  public Shop getById(Long id) {
    return shopRepository.findById(id).orElse(null);
  }

  @Override
  public List<Shop> list() {
    return shopRepository.findAll();
  }

  @Override
  public void save(Shop shop) {
    shopRepository.save(shop);
  }
}
