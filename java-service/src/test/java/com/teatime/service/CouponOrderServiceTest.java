package com.teatime.service;

import com.teatime.dto.Result;
import com.teatime.dto.UserDTO;
import com.teatime.entity.CouponOrder;
import com.teatime.service.IFollowService;
import com.teatime.mapper.CouponOrderMapper;
import com.teatime.service.impl.CouponOrderServiceImpl;
import com.teatime.utils.RedisIdGenerator;
import com.teatime.utils.UserHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponOrderServiceTest {

  @Mock
  private CouponOrderMapper couponOrderMapper;

  @Mock
  private IFlashSaleCouponService flashSaleCouponService;

  @Mock
  private RedisIdGenerator redisIdGenerator;

  @Mock
  private RedissonClient redissonClient;

  @Mock
  private StringRedisTemplate stringRedisTemplate;

  private CouponOrderServiceImpl couponOrderService;

  @BeforeEach
  void setUp() {
    couponOrderService = new CouponOrderServiceImpl(
        flashSaleCouponService,
        redisIdGenerator,
        redissonClient,
        stringRedisTemplate
    );
  }

  /**
   * Test 1: flashSaleCoupon - Successful purchase
   */
  @Test
  void testFlashSaleCoupon_Success() {
    // Arrange
    Long couponId = 1L;
    Long userId = 100L;
    long orderId = 123456L;

    when(redisIdGenerator.nextId("order")).thenReturn(orderId);

    // Mock Lua script returns 0 (success)
    when(stringRedisTemplate.execute(
        any(RedisScript.class),
        anyList(),
        anyString(),
        anyString(),
        anyString()
    )).thenReturn(0L);

    UserDTO currentUser = new UserDTO();
    currentUser.setId(userId);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class);
         MockedStatic<AopContext> aopMock = mockStatic(AopContext.class)) {

      userHolderMock.when(UserHolder::getUser).thenReturn(currentUser);

      // Mock AopContext.currentProxy() to return the service itself
      aopMock.when(AopContext::currentProxy).thenReturn(couponOrderService);

      // Act
      Result result = couponOrderService.flashSaleCoupon(couponId);

      // Assert
      assertTrue(result.getSuccess());
      assertEquals(orderId, result.getData());
    }
  }

  /**
   * Test 2: flashSaleCoupon - Insufficient stock (Lua script returns 1)
   */
  @Test
  void testFlashSaleCoupon_InsufficientStock() {
    // Arrange
    Long couponId = 1L;
    Long userId = 100L;
    long orderId = 123456L;

    when(redisIdGenerator.nextId("order")).thenReturn(orderId);

    // Mock Lua script returns 1 (insufficient stock)
    when(stringRedisTemplate.execute(
        any(RedisScript.class),
        eq(Collections.emptyList()),
        anyString(),
        anyString(),
        anyString()
    )).thenReturn(1L);

    UserDTO currentUser = new UserDTO();
    currentUser.setId(userId);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(currentUser);

      // Act
      Result result = couponOrderService.flashSaleCoupon(couponId);

      // Assert
      assertFalse(result.getSuccess());
      assertEquals("Insufficient stock", result.getErrorMsg());
    }
  }

  /**
   * Test 3: flashSaleCoupon - User already purchased (Lua script returns 2)
   */
  @Test
  void testFlashSaleCoupon_AlreadyPurchased() {
    // Arrange
    Long couponId = 1L;
    Long userId = 100L;
    long orderId = 123456L;

    when(redisIdGenerator.nextId("order")).thenReturn(orderId);

    // Mock Lua script returns 2 (user already purchased)
    when(stringRedisTemplate.execute(
        any(RedisScript.class),
        eq(Collections.emptyList()),
        anyString(),
        anyString(),
        anyString()
    )).thenReturn(2L);

    UserDTO currentUser = new UserDTO();
    currentUser.setId(userId);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(currentUser);

      // Act
      Result result = couponOrderService.flashSaleCoupon(couponId);

      // Assert
      assertFalse(result.getSuccess());
      assertEquals("You have already purchased this coupon", result.getErrorMsg());
    }
  }

  /**
   * Test 4: flashSaleCoupon - Unknown error (Lua script returns other value)
   */
  @Test
  void testFlashSaleCoupon_UnknownError() {
    // Arrange
    Long couponId = 1L;
    Long userId = 100L;
    long orderId = 123456L;

    when(redisIdGenerator.nextId("order")).thenReturn(orderId);

    // Mock Lua script returns 3 (unknown error)
    when(stringRedisTemplate.execute(
        any(RedisScript.class),
        eq(Collections.emptyList()),
        anyString(),
        anyString(),
        anyString()
    )).thenReturn(3L);

    UserDTO currentUser = new UserDTO();
    currentUser.setId(userId);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(currentUser);

      // Act
      Result result = couponOrderService.flashSaleCoupon(couponId);

      // Assert
      assertFalse(result.getSuccess());
      assertEquals("Flash Sale failed", result.getErrorMsg());
    }
  }
}