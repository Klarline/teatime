package com.teatime.service.impl;

import static com.teatime.utils.RedisConstants.CACHE_SHOP_TYPE_KEY;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
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

  @Resource
  private ShopTypeRepository shopTypeRepository;

  @Resource
  private org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

  @Override
  public Result queryTypeList() {
    String key = CACHE_SHOP_TYPE_KEY;

    String json = stringRedisTemplate.opsForValue().get(key);
    if (StrUtil.isNotBlank(json)) {
      List<ShopType> typeList = JSONUtil.toList(json, ShopType.class);
      return Result.ok(typeList);
    }

    List<ShopType> typeList = shopTypeRepository.findAll(Sort.by(Sort.Direction.ASC, "sort"));
    stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(typeList));

    return Result.ok(typeList);
  }
}
