package com.teatime.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.teatime.entity.FlashSaleCoupon;
import com.teatime.repository.FlashSaleCouponRepository;
import com.teatime.service.impl.FlashSaleCouponServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FlashSaleCouponServiceTest {

  @Mock
  private FlashSaleCouponRepository flashSaleCouponRepository;

  @InjectMocks
  private FlashSaleCouponServiceImpl flashSaleCouponService;

  @Test
  void testSave_CallsRepository() {
    FlashSaleCoupon coupon = new FlashSaleCoupon();
    coupon.setCouponId(1L);
    coupon.setStock(50);
    when(flashSaleCouponRepository.save(any(FlashSaleCoupon.class))).thenReturn(coupon);

    flashSaleCouponService.save(coupon);

    verify(flashSaleCouponRepository).save(coupon);
  }

  @Test
  void testDecrementStock_Success_ReturnsTrue() {
    when(flashSaleCouponRepository.decrementStock(1L)).thenReturn(1);

    boolean result = flashSaleCouponService.decrementStock(1L);

    assertTrue(result);
    verify(flashSaleCouponRepository).decrementStock(1L);
  }

  @Test
  void testDecrementStock_NoStock_ReturnsFalse() {
    when(flashSaleCouponRepository.decrementStock(1L)).thenReturn(0);

    boolean result = flashSaleCouponService.decrementStock(1L);

    assertFalse(result);
  }
}
