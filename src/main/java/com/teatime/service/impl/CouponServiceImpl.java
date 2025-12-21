package com.teatime.service.impl;

import static com.teatime.utils.RedisConstants.FLASH_SALE_STOCK_KEY;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teatime.dto.Result;
import com.teatime.entity.Coupon;
import com.teatime.entity.FlashSaleCoupon;
import com.teatime.mapper.CouponMapper;
import com.teatime.service.IFlashSaleCouponService;
import com.teatime.service.ICouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * Coupon service implementation
 * </p>
 *
 */
@Service
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon>
    implements ICouponService {

  private final IFlashSaleCouponService flashSaleCouponService;
  private final StringRedisTemplate stringRedisTemplate;

  @Override
  public Result queryCouponOfShop(Long shopId) {
    // query coupons by shop id
    List<Coupon> coupons = getBaseMapper().queryCouponOfShop(shopId);
    return Result.ok(coupons);
  }

  @Override
  @Transactional
  public void addFlashSaleCoupon(Coupon coupon) {
    // save coupon
    save(coupon);
    // save flash sale coupon information
    FlashSaleCoupon flashSaleCoupon = new FlashSaleCoupon();
    flashSaleCoupon.setCouponId(coupon.getId());
    flashSaleCoupon.setStock(coupon.getStock());
    flashSaleCoupon.setBeginTime(coupon.getBeginTime());
    flashSaleCoupon.setEndTime(coupon.getEndTime());
    flashSaleCouponService.save(flashSaleCoupon);

    // cache flash sale coupon stock in Redis
    stringRedisTemplate.opsForValue()
        .set(FLASH_SALE_STOCK_KEY + coupon.getId(), coupon.getStock().toString());
  }
}
