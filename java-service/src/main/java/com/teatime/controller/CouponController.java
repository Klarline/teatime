package com.teatime.controller;


import com.teatime.dto.Result;
import com.teatime.entity.Coupon;
import com.teatime.service.ICouponService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * Coupon controller
 * </p>
 */
@RestController
@RequestMapping("/api/coupon")
public class CouponController {

  @Resource
  private ICouponService couponService;

  /**
   * New coupon
   *
   * @param coupon coupon information
   * @return coupon id
   */
  @PostMapping
  public Result addCoupon(@RequestBody Coupon coupon) {
    couponService.save(coupon);
    return Result.ok(coupon.getId());
  }

  /**
   * New flash sale coupon
   *
   * @param coupon coupon information, including flash sale information
   * @return coupon id
   */
  @PostMapping("/flash-sale")
  public Result addFlashSaleCoupon(@RequestBody Coupon coupon) {
    couponService.addFlashSaleCoupon(coupon);
    return Result.ok(coupon.getId());
  }

  /**
   * Query coupons of shop
   *
   * @param shopId shop id
   * @return coupon list
   */
  @GetMapping("/list/{shopId}")
  public Result queryCouponOfShop(@PathVariable("shopId") Long shopId) {
    return couponService.queryCouponOfShop(shopId);
  }
}
