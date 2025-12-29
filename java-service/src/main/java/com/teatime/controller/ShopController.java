package com.teatime.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teatime.dto.Result;
import com.teatime.entity.Shop;
import com.teatime.service.IShopService;
import com.teatime.utils.SystemConstants;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * Front-end controller for shop management
 * </p>
 */
@RestController
@RequestMapping("/api/shop")
public class ShopController {

  @Resource
  public IShopService shopService;

  /**
   * Get shop information by ID
   * Get /api/shop/{id}
   * <p>
   * Returns: { "id": 1, "name": "Tea Time", ... }
   */
  @GetMapping("/{id}")
  public Result queryShopById(@PathVariable("id") Long id) {
    return shopService.queryById(id);
  }

  /**
   * Save new shop information
   * POST /api/shop
   */
  @PostMapping
  public Result saveShop(@RequestBody Shop shop) {
    shopService.save(shop);
    return Result.ok(shop.getId());
  }

  /**
   * Update existing shop information
   * PUT /api/shop
   */
  @PutMapping
  public Result updateShop(@RequestBody Shop shop) {
    return shopService.update(shop);
  }


  /**
   * Query shops by type with optional location sorting
   * GET /api/shop/of/type?typeId=1&current=1&x=123.45&y=67.89
   */
  @GetMapping("/of/type")
  public Result queryShopByType(
      @RequestParam("typeId") Integer typeId,
      @RequestParam(value = "current", defaultValue = "1") Integer current,
      @RequestParam(value = "x", required = false) Double x,
      @RequestParam(value = "y", required = false) Double y
  ) {
    return shopService.queryShopByType(typeId, current, x, y);
  }
  
  /**
   * Query shops by name with pagination
   * GET /api/shop/of/name?name=Tea&current=1
   */
  @GetMapping("/of/name")
  public Result queryShopByName(
      @RequestParam(value = "name", required = false) String name,
      @RequestParam(value = "current", defaultValue = "1") Integer current
  ) {
    // Query by name with pagination
    Page<Shop> page = shopService.query()
        .like(StrUtil.isNotBlank(name), "name", name)
        .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));

    return Result.ok(page.getRecords());
  }
}
