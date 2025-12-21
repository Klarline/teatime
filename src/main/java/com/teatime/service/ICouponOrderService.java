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

  Result flashSaleCoupon(Long couponId);

  void createCouponOrder(CouponOrder couponId);
}
