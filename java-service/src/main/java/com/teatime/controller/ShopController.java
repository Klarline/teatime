package com.teatime.controller;
import com.teatime.dto.Result;
import com.teatime.entity.Shop;
import com.teatime.service.IShopService;
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

  @GetMapping("/{id}")
  public Result queryShopById(@PathVariable("id") Long id) {
    return shopService.queryById(id);
  }

  @PostMapping
  public Result saveShop(@RequestBody Shop shop) {
    shopService.save(shop);
    return Result.ok(shop.getId());
  }

  @PutMapping
  public Result updateShop(@RequestBody Shop shop) {
    return shopService.update(shop);
  }

  @GetMapping("/of/type")
  public Result queryShopByType(
      @RequestParam("typeId") Integer typeId,
      @RequestParam(value = "current", defaultValue = "1") Integer current,
      @RequestParam(value = "x", required = false) Double x,
      @RequestParam(value = "y", required = false) Double y
  ) {
    return shopService.queryShopByType(typeId, current, x, y);
  }

  @GetMapping("/of/name")
  public Result queryShopByName(
      @RequestParam(value = "name", required = false) String name,
      @RequestParam(value = "current", defaultValue = "1") Integer current
  ) {
    return shopService.queryShopByName(name, current);
  }
}
