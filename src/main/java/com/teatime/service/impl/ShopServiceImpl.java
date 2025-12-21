package com.teatime.service.impl;

import static com.teatime.utils.RedisConstants.CACHE_SHOP_KEY;
import static com.teatime.utils.RedisConstants.CACHE_SHOP_TTL;
import static com.teatime.utils.RedisConstants.SHOP_GEO_KEY;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teatime.dto.Result;
import com.teatime.entity.Shop;
import com.teatime.mapper.ShopMapper;
import com.teatime.service.IShopService;
import com.teatime.utils.CacheClient;
import com.teatime.utils.SystemConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
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
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

  private static final ExecutorService CACHE_REBUILD_EXECUTOR =
      java.util.concurrent.Executors.newFixedThreadPool(10);

  @Resource
  private StringRedisTemplate stringRedisTemplate;

  @Resource
  private CacheClient cacheClient;

  @Override
  public Result queryById(Long id) {
    // solution 1: Cache penetration
    // Shop shop = cacheClient.queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);

    // solution 2: Cache breakdown with logical expiration
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
    // update database
    updateById(shop);

    // delete cache
    stringRedisTemplate.delete(CACHE_SHOP_KEY + shop.getId());
    return Result.ok();
  }

  @Override
  public Result queryShopByType(Integer typeId, Integer current, Double x, Double y) {
    if (x == null || y == null) {
      // no coordinates, query with pagination by database
      Page<Shop> page = query()
          .eq("type_id", typeId)
          .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
      return Result.ok(page.getRecords());
    }
    // with coordinates, use redis GEO to query
    int from = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
    int end = current * SystemConstants.DEFAULT_PAGE_SIZE;

    String key = SHOP_GEO_KEY + typeId;
    // query redis, sorted by distance
    GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo()
        .search(key, GeoReference.fromCoordinate(x, y), new Distance(5000),
            RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().limit(end));

    if (results == null) {
      return Result.ok(Collections.emptyList());
    }
    // parse shop ids
    List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
    if (list.size() <= from) {
      // no more data
      return Result.ok(Collections.emptyList());
    }

    List<Long> ids = new ArrayList<>(list.size());
    Map<String, Distance> distanceMap = new HashMap<>(list.size());
    list.stream().skip(from).forEach(geoResult -> {
      String shopIdStr = geoResult.getContent().getName();
      ids.add(Long.valueOf(shopIdStr));
      Distance distance = geoResult.getDistance();
      distanceMap.put(shopIdStr, distance);
    });

    List<Shop> shops =
        query().in("id", ids).last("ORDER BY FIELD(id," + StrUtil.join(",", ids) + ")")
            .list();
    for (Shop shop : shops) {
      shop.setDistance(
          distanceMap.get(shop.getId().toString()).getValue());
    }

    return Result.ok(shops);
  }
}
