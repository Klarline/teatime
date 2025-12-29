package com.teatime.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teatime.dto.Result;
import com.teatime.entity.Coupon;

/**
 * <p>
 * Coupon service interface
 * </p>
 */
public interface ICouponService extends IService<Coupon> {

  /**
   * Query coupons available for a specific shop
   *
   * @param shopId Shop ID
   * @return Result with coupon list
   */
  Result queryCouponOfShop(Long shopId);

  /**
   * Add a flash sale coupon
   *
   * @param coupon Coupon object
   */
  void addFlashSaleCoupon(Coupon coupon);
}
