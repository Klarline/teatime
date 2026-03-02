package com.teatime.service.impl;

import static com.teatime.utils.RedisConstants.FLASH_SALE_STOCK_KEY;

import com.teatime.dto.Result;
import com.teatime.entity.Coupon;
import com.teatime.entity.FlashSaleCoupon;
import com.teatime.repository.CouponRepository;
import com.teatime.service.IFlashSaleCouponService;
import com.teatime.service.ICouponService;
import com.teatime.utils.RedisFallback;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * Coupon service implementation
 * </p>
 */
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements ICouponService {

  private final CouponRepository couponRepository;
  private final IFlashSaleCouponService flashSaleCouponService;
  private final StringRedisTemplate stringRedisTemplate;

  @Override
  public Result queryCouponOfShop(Long shopId) {
    List<Coupon> coupons = couponRepository.findByShopIdAndStatusOrderByCreateTimeDesc(shopId, 1);
    return Result.ok(coupons);
  }

  @Override
  @Transactional
  public void addFlashSaleCoupon(Coupon coupon) {
    couponRepository.save(coupon);
    FlashSaleCoupon flashSaleCoupon = new FlashSaleCoupon();
    flashSaleCoupon.setCouponId(coupon.getId());
    flashSaleCoupon.setStock(coupon.getStock());
    flashSaleCoupon.setBeginTime(coupon.getBeginTime());
    flashSaleCoupon.setEndTime(coupon.getEndTime());
    flashSaleCouponService.save(flashSaleCoupon);

    RedisFallback.executeVoid(() ->
        stringRedisTemplate.opsForValue()
            .set(FLASH_SALE_STOCK_KEY + coupon.getId(), coupon.getStock().toString()));
  }

  @Override
  public void save(Coupon coupon) {
    couponRepository.save(coupon);
  }
}
