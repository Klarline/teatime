package com.teatime.controller;


import com.teatime.dto.Result;
import com.teatime.service.IShopTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * Front-end controller for shop types
 * </p>
 */
@RestController
@RequestMapping("/api/shop-type")
public class ShopTypeController {
  @Resource
  private IShopTypeService typeService;

  @GetMapping("/list")
  public Result queryTypeList() {
    return typeService.queryTypeList();
  }
}
