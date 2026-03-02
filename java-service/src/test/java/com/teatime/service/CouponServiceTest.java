package com.teatime.service;

import static com.teatime.utils.RedisConstants.FLASH_SALE_STOCK_KEY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.teatime.dto.Result;
import com.teatime.entity.Coupon;
import com.teatime.entity.FlashSaleCoupon;
import com.teatime.repository.CouponRepository;
import com.teatime.service.impl.CouponServiceImpl;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

  @Mock
  private CouponRepository couponRepository;

  @Mock
  private IFlashSaleCouponService flashSaleCouponService;

  @Mock
  private StringRedisTemplate stringRedisTemplate;

  @Mock
  private ValueOperations<String, String> valueOperations;

  private CouponServiceImpl couponService;

  @BeforeEach
  void setUp() {
    lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    couponService = new CouponServiceImpl(
        couponRepository,
        flashSaleCouponService,
        stringRedisTemplate
    );
  }

  @Test
  void testQueryCouponOfShop_ReturnsCoupons() {
    Long shopId = 1L;
    List<Coupon> coupons = Arrays.asList(createTestCoupon(1L, shopId), createTestCoupon(2L, shopId));
    when(couponRepository.findByShopIdAndStatusOrderByCreateTimeDesc(shopId, 1))
        .thenReturn(coupons);

    Result result = couponService.queryCouponOfShop(shopId);

    assertTrue(result.getSuccess());
    @SuppressWarnings("unchecked")
    List<Coupon> resultList = (List<Coupon>) result.getData();
    assertEquals(2, resultList.size());
    assertEquals(1L, resultList.get(0).getId());
    verify(couponRepository).findByShopIdAndStatusOrderByCreateTimeDesc(shopId, 1);
  }

  @Test
  void testQueryCouponOfShop_EmptyList() {
    Long shopId = 99L;
    when(couponRepository.findByShopIdAndStatusOrderByCreateTimeDesc(shopId, 1))
        .thenReturn(List.of());

    Result result = couponService.queryCouponOfShop(shopId);

    assertTrue(result.getSuccess());
    @SuppressWarnings("unchecked")
    List<Coupon> resultList = (List<Coupon>) result.getData();
    assertTrue(resultList.isEmpty());
  }

  @Test
  void testAddFlashSaleCoupon_SavesCouponAndFlashSaleAndRedis() {
    Coupon coupon = createTestCoupon(null, 1L);
    coupon.setStock(50);
    coupon.setBeginTime(LocalDateTime.now());
    coupon.setEndTime(LocalDateTime.now().plusHours(2));

    when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> {
      Coupon c = invocation.getArgument(0);
      c.setId(1L);
      return c;
    });

    couponService.addFlashSaleCoupon(coupon);

    verify(couponRepository).save(coupon);
    ArgumentCaptor<FlashSaleCoupon> flashSaleCaptor = ArgumentCaptor.forClass(FlashSaleCoupon.class);
    verify(flashSaleCouponService).save(flashSaleCaptor.capture());
    FlashSaleCoupon savedFlashSale = flashSaleCaptor.getValue();
    assertEquals(1L, savedFlashSale.getCouponId());
    assertEquals(50, savedFlashSale.getStock());

    verify(valueOperations).set(eq(FLASH_SALE_STOCK_KEY + 1L), eq("50"));
  }

  @Test
  void testSave_CallsRepository() {
    Coupon coupon = createTestCoupon(null, 1L);
    when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);

    couponService.save(coupon);

    verify(couponRepository).save(coupon);
  }

  private Coupon createTestCoupon(Long id, Long shopId) {
    Coupon coupon = new Coupon();
    coupon.setId(id);
    coupon.setShopId(shopId);
    coupon.setTitle("Test Coupon");
    coupon.setStatus(1);
    return coupon;
  }
}
