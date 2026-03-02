package com.teatime.service;

import com.teatime.dto.Result;
import com.teatime.entity.Shop;

import java.util.List;

/**
 * <p>
 * Service interface for shop management
 * </p>
 */
public interface IShopService {

  Result queryById(Long id);

  void save(Shop shop);

  Result update(Shop shop);

  Result queryShopByType(Integer typeId, Integer current, Double x, Double y);

  Result queryShopByName(String name, Integer current);

  Shop getById(Long id);

  List<Shop> list();
}
