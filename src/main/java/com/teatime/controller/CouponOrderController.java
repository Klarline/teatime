package com.teatime.controller;


import com.teatime.dto.Result;
import com.teatime.service.ICouponOrderService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Coupon order controller
 * </p>
 */
@RestController
@RequestMapping("/api/coupon-order")
public class CouponOrderController {

  private ICouponOrderService couponOrderService;

  public CouponOrderController(ICouponOrderService couponOrderService) {
    this.couponOrderService = couponOrderService;
  }

  @PostMapping("/flash-sale/{id}")
  public Result flashSaleCoupon(@PathVariable("id") Long couponId) {
    return couponOrderService.flashSaleCoupon(couponId);
  }
}
