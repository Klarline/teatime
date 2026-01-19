package com.teatime.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teatime.dto.Result;
import com.teatime.entity.CouponOrder;
import com.teatime.mapper.CouponOrderMapper;
import com.teatime.service.IFlashSaleCouponService;
import com.teatime.service.ICouponOrderService;
import com.teatime.utils.RedisIdGenerator;
import com.teatime.utils.UserHolder;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Coupon order service implementation
 * </p>
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponOrderServiceImpl extends ServiceImpl<CouponOrderMapper, CouponOrder>
    implements ICouponOrderService {

  private static final DefaultRedisScript<Long> FLASH_SALE_SCRIPT;
  private static final ExecutorService FLASH_SALE_ORDER_EXECUTOR =
      Executors.newSingleThreadExecutor();

  static {
    FLASH_SALE_SCRIPT = new DefaultRedisScript<>();
    FLASH_SALE_SCRIPT.setLocation(new ClassPathResource("flashSale.lua"));
    FLASH_SALE_SCRIPT.setResultType(Long.class);
  }

  private final IFlashSaleCouponService flashSaleCouponService;
  private final RedisIdGenerator redisIdGenerator;
  private final RedissonClient redissonClient;
  private final StringRedisTemplate stringRedisTemplate;
  private ICouponOrderService proxy;

  @PostConstruct
  public void init() {
    FLASH_SALE_ORDER_EXECUTOR.submit(new CouponOrderHandler());
  }

  /**
   * Handle flash sale coupon purchase request
   *
   * @param couponId the coupon id
   * @return Result containing order id or error message
   */
  @Override
  public Result flashSaleCoupon(Long couponId) {
    Long userId = UserHolder.getUser().getId();
    // generate order id
    long orderId = redisIdGenerator.nextId("order");

    // check flash sale conditions, deduct stock and  send order to queue atomically using Lua script
    Long result =
        stringRedisTemplate.execute(FLASH_SALE_SCRIPT, Collections.emptyList(),
            String.valueOf(couponId),
            String.valueOf(userId), String.valueOf(orderId));

    int res = result.intValue();
    if (res != 0) {
      return switch (res) {
        case 1 -> Result.fail("Insufficient stock");
        case 2 -> Result.fail("You have already purchased this coupon");
        default -> Result.fail("Flash Sale failed");
      };
    }


    // get proxy object to call transactional method
    proxy = (ICouponOrderService) AopContext.currentProxy();

    return Result.ok(orderId);
  }

  // process coupon order
  private void handleCouponOrder(CouponOrder couponOrder) {
    Long userId = couponOrder.getUserId();
    RLock lock = redissonClient.getLock("lock:order:" + userId);
    boolean isLock = lock.tryLock();
    if (!isLock) {
      // failed to acquire lock, log error
      log.error("Duplicate order attempt for user {}", userId);
      return;
    }
    try {
      proxy.createCouponOrder(couponOrder);
    } finally {
      lock.unlock();
    }
  }

  /**
   * Create coupon order with transactional support
   *
   * @param couponOrder the coupon order
   */
  @Transactional
  public void createCouponOrder(CouponOrder couponOrder) {
    // check if user has already purchased
    Long userId = couponOrder.getUserId();
    Long count =
        query().eq("user_id", userId).eq("coupon_id", couponOrder.getCouponId()).count();
    if (count > 0) {
      log.error("User {} has already purchased coupon {}", userId,
          couponOrder.getCouponId());
      return;
    }
    // deduct stock
    boolean success = flashSaleCouponService.update().setSql("stock = stock - 1")
        .eq("coupon_id", couponOrder.getCouponId()).gt("stock", 0).update();
    if (!success) {
      log.error("Insufficient stock for coupon");
      return;
    }

    // save order
    this.save(couponOrder);
  }

  // background handler for processing coupon orders from Redis stream
  private class CouponOrderHandler implements Runnable {
    String queueName = "stream.orders";

    /**
     * Run the coupon order handler
     */
    @Override
    public void run() {
      while (true) {
        try {
          // read order from stream
          List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
              Consumer.from("g1", "c1"),
              StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
              StreamOffset.create(queueName, ReadOffset.lastConsumed())
          );

          // if no new orders, continue
          if (list == null || list.isEmpty()) {
            // no new orders, continue
            continue;
          }

          // process order
          MapRecord<String, Object, Object> record = list.get(0);
          Map<Object, Object> values = record.getValue();
          CouponOrder couponOrder = BeanUtil.fillBeanWithMap(values, new CouponOrder(), true);
          handleCouponOrder(couponOrder);

          // acknowledge message
          stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
        } catch (Exception e) {
          log.error("Error processing coupon order", e);
          handlePendingList();
        }
      }
    }

    // handle pending list for unacknowledged messages
    private void handlePendingList() {
      while (true) {
        try {
          // read order from pending list
          List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
              Consumer.from("g1", "c1"),
              StreamReadOptions.empty().count(1),
              StreamOffset.create(queueName, ReadOffset.from("0"))
          );

          // if no pending orders, break
          if (list == null || list.isEmpty()) {
            break;
          }

          // process order
          MapRecord<String, Object, Object> record = list.get(0);
          Map<Object, Object> values = record.getValue();
          CouponOrder couponOrder = BeanUtil.fillBeanWithMap(values, new CouponOrder(), true);
          handleCouponOrder(couponOrder);

          // acknowledge message
          stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
        } catch (Exception e) {
          log.error("Error processing pending coupon order", e);
          try {
            Thread.sleep(20); // wait before retrying
          } catch (InterruptedException ex) {
            ex.printStackTrace();
          }
        }
      }
    }
  }
}
