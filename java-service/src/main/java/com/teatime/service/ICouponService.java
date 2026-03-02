package com.teatime.service;

import com.teatime.dto.Result;
import com.teatime.entity.Coupon;

/**
 * <p>
 * Coupon service interface
 * </p>
 */
public interface ICouponService {

  Result queryCouponOfShop(Long shopId);

  void addFlashSaleCoupon(Coupon coupon);

  void save(Coupon coupon);
}
