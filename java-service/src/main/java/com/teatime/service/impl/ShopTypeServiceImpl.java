package com.teatime.service.impl;

import static com.teatime.utils.RedisConstants.CACHE_SHOP_TYPE_KEY;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teatime.utils.RedisFallback;
import org.apache.commons.lang3.StringUtils;
import com.teatime.dto.Result;
import com.teatime.entity.ShopType;
import com.teatime.repository.ShopTypeRepository;
import com.teatime.service.IShopTypeService;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Service implementation for shop types
 * </p>
 */
@Service
public class ShopTypeServiceImpl implements IShopTypeService {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Resource
  private ShopTypeRepository shopTypeRepository;

  @Resource
  private org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

  @Override
  public Result queryTypeList() {
    String key = CACHE_SHOP_TYPE_KEY;

    String json = RedisFallback.execute(
        () -> stringRedisTemplate.opsForValue().get(key),
        () -> null
    );
    if (StringUtils.isNotBlank(json)) {
      try {
        List<ShopType> typeList = OBJECT_MAPPER.readValue(json,
            OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, ShopType.class));
        return Result.ok(typeList);
      } catch (Exception e) {
        throw new RuntimeException("Failed to deserialize shop types", e);
      }
    }

    List<ShopType> typeList = shopTypeRepository.findAll(Sort.by(Sort.Direction.ASC, "sort"));
    RedisFallback.executeVoid(() -> {
      try {
        stringRedisTemplate.opsForValue().set(key, OBJECT_MAPPER.writeValueAsString(typeList));
      } catch (Exception e) {
        throw new RuntimeException("Failed to serialize shop types", e);
      }
    });

    return Result.ok(typeList);
  }
}
