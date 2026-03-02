package com.teatime.service.impl;

import com.teatime.entity.FlashSaleCoupon;
import com.teatime.repository.FlashSaleCouponRepository;
import com.teatime.service.IFlashSaleCouponService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * Flash Sale coupon service implementation
 * </p>
 */
@Service
public class FlashSaleCouponServiceImpl implements IFlashSaleCouponService {

  @Resource
  private FlashSaleCouponRepository flashSaleCouponRepository;

  @Override
  public void save(FlashSaleCoupon flashSaleCoupon) {
    flashSaleCouponRepository.save(flashSaleCoupon);
  }

  @Override
  public boolean decrementStock(Long couponId) {
    int updated = flashSaleCouponRepository.decrementStock(couponId);
    return updated > 0;
  }
}
