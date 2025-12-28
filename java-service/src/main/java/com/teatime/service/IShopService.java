package com.teatime.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teatime.dto.Result;
import com.teatime.entity.Shop;

/**
 * <p>
 * Service interface for shop management
 * </p>
 */
public interface IShopService extends IService<Shop> {

  Result queryById(Long id);

  Result update(Shop shop);

  Result queryShopByType(Integer typeId, Integer current, Double x, Double y);
}
