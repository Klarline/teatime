package com.teatime.service;

import com.teatime.entity.FlashSaleCoupon;

/**
 * <p>
 * Flash Sale coupon service interface
 * </p>
 */
public interface IFlashSaleCouponService {

  void save(FlashSaleCoupon flashSaleCoupon);

  boolean decrementStock(Long couponId);
}
