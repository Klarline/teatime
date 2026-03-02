package com.teatime.service;

import com.teatime.dto.Result;
import com.teatime.dto.UserDTO;
import com.teatime.entity.CouponOrder;
import com.teatime.repository.CouponOrderRepository;
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
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponOrderServiceTest {

  @Mock
  private CouponOrderRepository couponOrderRepository;

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
        couponOrderRepository,
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
  @SuppressWarnings("unchecked")
  void testFlashSaleCoupon_Success() {
    Long couponId = 1L;
    Long userId = 100L;
    long orderId = 123456L;

    when(redisIdGenerator.nextId("order")).thenReturn(orderId);

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

      aopMock.when(AopContext::currentProxy).thenReturn(couponOrderService);

      Result result = couponOrderService.flashSaleCoupon(couponId);

      assertTrue(result.getSuccess());
      assertEquals(orderId, result.getData());
    }
  }

  /**
   * Test 2: flashSaleCoupon - Insufficient stock (Lua script returns 1)
   */
  @Test
  @SuppressWarnings("unchecked")
  void testFlashSaleCoupon_InsufficientStock() {
    Long couponId = 1L;
    Long userId = 100L;
    long orderId = 123456L;

    when(redisIdGenerator.nextId("order")).thenReturn(orderId);

    when(stringRedisTemplate.execute(
        any(RedisScript.class),
        eq(Collections.emptyList()),
        anyString(),
        anyString(),
        anyString()
    )).thenReturn(1L);

    UserDTO currentUser = new UserDTO();
    currentUser.setId(userId);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class);
         MockedStatic<AopContext> aopMock = mockStatic(AopContext.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(currentUser);
      aopMock.when(AopContext::currentProxy).thenReturn(couponOrderService);

      Result result = couponOrderService.flashSaleCoupon(couponId);

      assertFalse(result.getSuccess());
      assertEquals("Insufficient stock", result.getErrorMsg());
    }
  }

  /**
   * Test 3: flashSaleCoupon - User already purchased (Lua script returns 2)
   */
  @Test
  @SuppressWarnings("unchecked")
  void testFlashSaleCoupon_AlreadyPurchased() {
    Long couponId = 1L;
    Long userId = 100L;
    long orderId = 123456L;

    when(redisIdGenerator.nextId("order")).thenReturn(orderId);

    when(stringRedisTemplate.execute(
        any(RedisScript.class),
        eq(Collections.emptyList()),
        anyString(),
        anyString(),
        anyString()
    )).thenReturn(2L);

    UserDTO currentUser = new UserDTO();
    currentUser.setId(userId);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class);
         MockedStatic<AopContext> aopMock = mockStatic(AopContext.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(currentUser);
      aopMock.when(AopContext::currentProxy).thenReturn(couponOrderService);

      Result result = couponOrderService.flashSaleCoupon(couponId);

      assertFalse(result.getSuccess());
      assertEquals("You have already purchased this coupon", result.getErrorMsg());
    }
  }

  /**
   * Test 4: flashSaleCoupon - Unknown error (Lua script returns other value)
   */
  @Test
  @SuppressWarnings("unchecked")
  void testFlashSaleCoupon_UnknownError() {
    Long couponId = 1L;
    Long userId = 100L;
    long orderId = 123456L;

    when(redisIdGenerator.nextId("order")).thenReturn(orderId);

    when(stringRedisTemplate.execute(
        any(RedisScript.class),
        eq(Collections.emptyList()),
        anyString(),
        anyString(),
        anyString()
    )).thenReturn(3L);

    UserDTO currentUser = new UserDTO();
    currentUser.setId(userId);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class);
         MockedStatic<AopContext> aopMock = mockStatic(AopContext.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(currentUser);
      aopMock.when(AopContext::currentProxy).thenReturn(couponOrderService);

      Result result = couponOrderService.flashSaleCoupon(couponId);

      assertFalse(result.getSuccess());
      assertEquals("Flash Sale failed", result.getErrorMsg());
    }
  }

  /**
   * Test 4b: flashSaleCoupon - Redis down, sync path succeeds
   */
  @Test
  @SuppressWarnings("unchecked")
  void testFlashSaleCoupon_RedisDown_UsesSyncPath_Success() {
    Long couponId = 1L;
    Long userId = 100L;
    long orderId = 123456L;

    when(redisIdGenerator.nextId("order")).thenReturn(orderId);
    when(stringRedisTemplate.execute(
        any(RedisScript.class),
        anyList(),
        anyString(),
        anyString(),
        anyString()
    )).thenThrow(new DataAccessException("Connection refused") {});

    when(couponOrderRepository.countByUserIdAndCouponId(userId, couponId)).thenReturn(0L);
    when(flashSaleCouponService.decrementStock(couponId)).thenReturn(true);

    UserDTO currentUser = new UserDTO();
    currentUser.setId(userId);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class);
         MockedStatic<AopContext> aopMock = mockStatic(AopContext.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(currentUser);
      aopMock.when(AopContext::currentProxy).thenReturn(couponOrderService);

      Result result = couponOrderService.flashSaleCoupon(couponId);

      assertTrue(result.getSuccess());
      assertEquals(orderId, result.getData());
      verify(couponOrderRepository).save(argThat(order ->
          order.getUserId().equals(userId) && order.getCouponId().equals(couponId)));
    }
  }

  /**
   * Test 4c: flashSaleCoupon - Redis down, sync path insufficient stock
   */
  @Test
  @SuppressWarnings("unchecked")
  void testFlashSaleCoupon_RedisDown_UsesSyncPath_InsufficientStock() {
    Long couponId = 1L;
    Long userId = 100L;
    long orderId = 123456L;

    when(redisIdGenerator.nextId("order")).thenReturn(orderId);
    when(stringRedisTemplate.execute(
        any(RedisScript.class),
        anyList(),
        anyString(),
        anyString(),
        anyString()
    )).thenThrow(new DataAccessException("Connection refused") {});

    when(couponOrderRepository.countByUserIdAndCouponId(userId, couponId)).thenReturn(0L);
    when(flashSaleCouponService.decrementStock(couponId)).thenReturn(false);

    UserDTO currentUser = new UserDTO();
    currentUser.setId(userId);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class);
         MockedStatic<AopContext> aopMock = mockStatic(AopContext.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(currentUser);
      aopMock.when(AopContext::currentProxy).thenReturn(couponOrderService);

      Result result = couponOrderService.flashSaleCoupon(couponId);

      assertFalse(result.getSuccess());
      assertEquals("Insufficient stock", result.getErrorMsg());
      verify(couponOrderRepository, never()).save(any());
    }
  }

  /**
   * Test 5: createCouponOrder - Success
   */
  @Test
  void testCreateCouponOrder_Success() {
    CouponOrder order = new CouponOrder();
    order.setUserId(100L);
    order.setCouponId(1L);

    when(couponOrderRepository.countByUserIdAndCouponId(100L, 1L)).thenReturn(0L);
    when(flashSaleCouponService.decrementStock(1L)).thenReturn(true);

    boolean result = couponOrderService.createCouponOrder(order);

    assertTrue(result);
    verify(couponOrderRepository).save(order);
    verify(flashSaleCouponService).decrementStock(1L);
  }

  /**
   * Test 6: createCouponOrder - Already purchased, does not save
   */
  @Test
  void testCreateCouponOrder_AlreadyPurchased_DoesNotSave() {
    CouponOrder order = new CouponOrder();
    order.setUserId(100L);
    order.setCouponId(1L);

    when(couponOrderRepository.countByUserIdAndCouponId(100L, 1L)).thenReturn(1L);

    boolean result = couponOrderService.createCouponOrder(order);

    assertFalse(result);
    verify(couponOrderRepository, never()).save(any());
    verify(flashSaleCouponService, never()).decrementStock(anyLong());
  }

  /**
   * Test 7: createCouponOrder - Insufficient stock, does not save
   */
  @Test
  void testCreateCouponOrder_InsufficientStock_DoesNotSave() {
    CouponOrder order = new CouponOrder();
    order.setUserId(100L);
    order.setCouponId(1L);

    when(couponOrderRepository.countByUserIdAndCouponId(100L, 1L)).thenReturn(0L);
    when(flashSaleCouponService.decrementStock(1L)).thenReturn(false);

    boolean result = couponOrderService.createCouponOrder(order);

    assertFalse(result);
    verify(couponOrderRepository, never()).save(any());
  }

  /**
   * Test 8: handleCouponOrder - Successfully processes order (via reflection)
   */
  @Test
  void testHandleCouponOrder_Success() throws Exception {
    CouponOrder order = new CouponOrder();
    order.setUserId(100L);
    order.setCouponId(1L);

    org.redisson.api.RLock mockLock = mock(org.redisson.api.RLock.class);
    when(redissonClient.getLock("lock:order:100")).thenReturn(mockLock);
    when(mockLock.tryLock()).thenReturn(true);

    when(couponOrderRepository.countByUserIdAndCouponId(100L, 1L)).thenReturn(0L);
    when(flashSaleCouponService.decrementStock(1L)).thenReturn(true);

    org.springframework.test.util.ReflectionTestUtils.setField(couponOrderService, "proxy", couponOrderService);

    java.lang.reflect.Method method = com.teatime.service.impl.CouponOrderServiceImpl.class
        .getDeclaredMethod("handleCouponOrder", CouponOrder.class);
    method.setAccessible(true);
    method.invoke(couponOrderService, order);

    verify(couponOrderRepository).save(order);
    verify(mockLock).unlock();
  }

  /**
   * Test 9: handleCouponOrder - Lock fails, does not process
   */
  @Test
  void testHandleCouponOrder_LockFails_DoesNotProcess() throws Exception {
    CouponOrder order = new CouponOrder();
    order.setUserId(100L);
    order.setCouponId(1L);

    org.redisson.api.RLock mockLock = mock(org.redisson.api.RLock.class);
    when(redissonClient.getLock("lock:order:100")).thenReturn(mockLock);
    when(mockLock.tryLock()).thenReturn(false);

    org.springframework.test.util.ReflectionTestUtils.setField(couponOrderService, "proxy", couponOrderService);

    java.lang.reflect.Method method = com.teatime.service.impl.CouponOrderServiceImpl.class
        .getDeclaredMethod("handleCouponOrder", CouponOrder.class);
    method.setAccessible(true);
    method.invoke(couponOrderService, order);

    verify(couponOrderRepository, never()).save(any());
  }
}
