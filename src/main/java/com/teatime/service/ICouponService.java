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

  Result queryCouponOfShop(Long shopId);

  void addFlashSaleCoupon(Coupon coupon);
}
