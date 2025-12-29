package com.teatime.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teatime.dto.Result;
import com.teatime.entity.CouponOrder;

/**
 * <p>
 * Coupon order service
 * </p>
 *
 */
public interface ICouponOrderService extends IService<CouponOrder> {

  /**
   * Flash sale coupon
   *
   * @param couponId Coupon ID
   * @return Result
   */
  Result flashSaleCoupon(Long couponId);

  /**
   * Create coupon order
   *
   * @param couponId Coupon ID
   */
  void createCouponOrder(CouponOrder couponId);
}
