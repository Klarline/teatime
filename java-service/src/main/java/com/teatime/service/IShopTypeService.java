package com.teatime.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teatime.dto.Result;
import com.teatime.entity.ShopType;

/**
 * <p>
 * Service interface for shop types
 * </p>
 */
public interface IShopTypeService extends IService<ShopType> {

  /**
   * Retrieve the list of shop types
   *
   * @return Result containing the list of shop types
   */
  Result queryTypeList();
}
