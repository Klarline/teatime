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
   * Add a new coupon
   * POST /api/coupon
   */
  @PostMapping
  public Result addCoupon(@RequestBody Coupon coupon) {
    couponService.save(coupon);
    return Result.ok(coupon.getId());
  }

  /**
   * Add a flash sale coupon
   * POST /api/coupon/flash-sale
   */
  @PostMapping("/flash-sale")
  public Result addFlashSaleCoupon(@RequestBody Coupon coupon) {
    couponService.addFlashSaleCoupon(coupon);
    return Result.ok(coupon.getId());
  }

  /**
   * Get coupons for a specific shop
   * GET /api/coupon/list/{shopId}
   */
  @GetMapping("/list/{shopId}")
  public Result queryCouponOfShop(@PathVariable("shopId") Long shopId) {
    return couponService.queryCouponOfShop(shopId);
  }
}
