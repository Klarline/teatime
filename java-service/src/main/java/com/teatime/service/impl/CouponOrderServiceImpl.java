package com.teatime.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.teatime.dto.Result;
import com.teatime.entity.CouponOrder;
import com.teatime.repository.CouponOrderRepository;
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
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponOrderServiceImpl implements ICouponOrderService {

  private static final DefaultRedisScript<Long> FLASH_SALE_SCRIPT;
  private static final ExecutorService FLASH_SALE_ORDER_EXECUTOR =
      Executors.newSingleThreadExecutor();

  static {
    FLASH_SALE_SCRIPT = new DefaultRedisScript<>();
    FLASH_SALE_SCRIPT.setLocation(new ClassPathResource("flashSale.lua"));
    FLASH_SALE_SCRIPT.setResultType(Long.class);
  }

  private final CouponOrderRepository couponOrderRepository;
  private final IFlashSaleCouponService flashSaleCouponService;
  private final RedisIdGenerator redisIdGenerator;
  private final RedissonClient redissonClient;
  private final StringRedisTemplate stringRedisTemplate;
  private ICouponOrderService proxy;

  @PostConstruct
  public void init() {
    FLASH_SALE_ORDER_EXECUTOR.submit(() -> {
      try {
        try {
          stringRedisTemplate.opsForStream()
              .createGroup("stream.orders", "g1");
          log.info("Created Redis Stream consumer group 'g1' for 'stream.orders'");
        } catch (Exception e) {
          log.info("Consumer group 'g1' already exists or will be created on first message");
        }

        new CouponOrderHandler().run();
      } catch (Exception e) {
        log.error("Error initializing coupon order handler: {}", e.getMessage());
      }
    });
  }

  @Override
  public Result flashSaleCoupon(Long couponId) {
    Long userId = UserHolder.getUser().getId();
    long orderId = redisIdGenerator.nextId("order");

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

    proxy = (ICouponOrderService) AopContext.currentProxy();

    return Result.ok(orderId);
  }

  private void handleCouponOrder(CouponOrder couponOrder) {
    Long userId = couponOrder.getUserId();
    RLock lock = redissonClient.getLock("lock:order:" + userId);
    boolean isLock = lock.tryLock();
    if (!isLock) {
      log.error("Duplicate order attempt for user {}", userId);
      return;
    }
    try {
      proxy.createCouponOrder(couponOrder);
    } finally {
      lock.unlock();
    }
  }

  @Override
  @Transactional
  public void createCouponOrder(CouponOrder couponOrder) {
    Long userId = couponOrder.getUserId();
    long count = couponOrderRepository.countByUserIdAndCouponId(userId, couponOrder.getCouponId());
    if (count > 0) {
      log.error("User {} has already purchased coupon {}", userId,
          couponOrder.getCouponId());
      return;
    }

    boolean success = flashSaleCouponService.decrementStock(couponOrder.getCouponId());
    if (!success) {
      log.error("Insufficient stock for coupon");
      return;
    }

    couponOrderRepository.save(couponOrder);
  }

  private class CouponOrderHandler implements Runnable {
    String queueName = "stream.orders";

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
      while (true) {
        try {
          List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
              Consumer.from("g1", "c1"),
              StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
              StreamOffset.create(queueName, ReadOffset.lastConsumed())
          );

          if (list == null || list.isEmpty()) {
            continue;
          }

          MapRecord<String, Object, Object> record = list.get(0);
          Map<Object, Object> values = record.getValue();
          CouponOrder couponOrder = BeanUtil.fillBeanWithMap(values, new CouponOrder(), true);
          handleCouponOrder(couponOrder);

          stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
        } catch (Exception e) {
          log.error("Error processing coupon order", e);
          handlePendingList();
        }
      }
    }

    @SuppressWarnings("unchecked")
    private void handlePendingList() {
      while (true) {
        try {
          List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
              Consumer.from("g1", "c1"),
              StreamReadOptions.empty().count(1),
              StreamOffset.create(queueName, ReadOffset.from("0"))
          );

          if (list == null || list.isEmpty()) {
            break;
          }

          MapRecord<String, Object, Object> record = list.get(0);
          Map<Object, Object> values = record.getValue();
          CouponOrder couponOrder = BeanUtil.fillBeanWithMap(values, new CouponOrder(), true);
          handleCouponOrder(couponOrder);

          stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
        } catch (Exception e) {
          log.error("Error processing pending coupon order", e);
          try {
            Thread.sleep(20);
          } catch (InterruptedException ex) {
            ex.printStackTrace();
          }
        }
      }
    }
  }
}
