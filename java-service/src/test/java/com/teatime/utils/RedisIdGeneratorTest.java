package com.teatime.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisIdGeneratorTest {

  @Mock
  private StringRedisTemplate stringRedisTemplate;

  @Mock
  private ValueOperations<String, String> valueOperations;

  private RedisIdGenerator redisIdGenerator;

  @BeforeEach
  void setUp() {
    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.increment(anyString())).thenReturn(1L);

    redisIdGenerator = new RedisIdGenerator(stringRedisTemplate);
  }

  @Test
  void testNextId_ReturnsPositiveId() {
    long id = redisIdGenerator.nextId("order");

    assertTrue(id > 0);
  }

  @Test
  void testNextId_WithIncrementingSequence_ReturnsDifferentIds() {
    when(valueOperations.increment(anyString())).thenReturn(1L, 2L);

    long id1 = redisIdGenerator.nextId("order");
    long id2 = redisIdGenerator.nextId("order");

    assertTrue(id1 > 0);
    assertTrue(id2 > 0);
    assertNotEquals(id1, id2);
  }

  @Test
  void testNextId_IncludesKeyPrefix() {
    redisIdGenerator.nextId("order");

    verify(valueOperations).increment(argThat(key -> key.startsWith("icrorder")));
  }
}
