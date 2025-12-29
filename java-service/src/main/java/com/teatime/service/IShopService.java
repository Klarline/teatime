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

  /**
   * Query shop by ID
   *
   * @param id Shop ID
   * @return Result with shop details
   */
  Result queryById(Long id);

  /**
   * Update shop information
   *
   * @param shop Shop object with updated information
   * @return Result of the update operation
   */
  Result update(Shop shop);

  /**
   * Query shops by type with pagination and optional location
   *
   * @param typeId  Shop type ID
   * @param current Current page number
   * @param x       Longitude (optional)
   * @param y       Latitude (optional)
   * @return Result with list of shops
   */
  Result queryShopByType(Integer typeId, Integer current, Double x, Double y);
}
