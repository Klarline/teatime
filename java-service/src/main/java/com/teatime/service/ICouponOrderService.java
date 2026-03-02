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

  /**
   * Creates a coupon order. Must be called within a transactional context.
   *
   * @return true if order was created, false if duplicate purchase or insufficient stock
   */
  boolean createCouponOrder(CouponOrder couponOrder);
}
