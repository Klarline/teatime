package com.teatime.service;

import com.teatime.dto.Result;
import com.teatime.entity.CouponOrder;

/**
 * <p>
 * Coupon order service
 * </p>
 *
 */
public interface ICouponOrderService {

  Result flashSaleCoupon(Long couponId);

  void createCouponOrder(CouponOrder couponOrder);
}
