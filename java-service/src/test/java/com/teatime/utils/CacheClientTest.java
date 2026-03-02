package com.teatime.utils;

import cn.hutool.json.JSONUtil;
import com.teatime.entity.Shop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheClientTest {

  @Mock
  private StringRedisTemplate stringRedisTemplate;

  @Mock
  private ValueOperations<String, String> valueOperations;

  private CacheClient cacheClient;

  @BeforeEach
  void setUp() {
    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    cacheClient = new CacheClient(stringRedisTemplate);
  }

  /**
   * Test 1: query() - Cache hit returns cached data
   */
  @Test
  @SuppressWarnings("unchecked")
  void testQuery_CacheHit_ReturnsData() {
    // Arrange
    Shop shop = createTestShop(1L, "Test Shop");
    String key = "cache:shop:1";
    when(valueOperations.get(key)).thenReturn(JSONUtil.toJsonStr(shop));

    Function<Long, Shop> dbFallback = mock(Function.class);

    // Act
    Shop result =
        cacheClient.query("cache:shop:", 1L, Shop.class, dbFallback, 30L, TimeUnit.MINUTES);

    // Assert
    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("Test Shop", result.getName());
    verify(dbFallback, never()).apply(any()); // Database should NOT be called
  }

  /**
   * Test 2: query() - Cache miss queries database and caches result
   */
  @Test
  void testQuery_CacheMiss_QueriesDatabaseAndCaches() {
    // Arrange
    String key = "cache:shop:1";
    Shop shop = createTestShop(1L, "Test Shop");

    when(valueOperations.get(key)).thenReturn(null);
    Function<Long, Shop> dbFallback = id -> shop;

    // Act
    Shop result =
        cacheClient.query("cache:shop:", 1L, Shop.class, dbFallback, 30L, TimeUnit.MINUTES);

    // Assert
    assertNotNull(result);
    assertEquals(1L, result.getId());

    // Verify data was cached
    verify(valueOperations).set(
        eq(key),
        eq(JSONUtil.toJsonStr(shop)),
        eq(30L),
        eq(TimeUnit.MINUTES)
    );
  }

  /**
   * Test 3: queryWithPassThrough() - Cache hit returns cached data
   */
  @Test
  @SuppressWarnings("unchecked")
  void testQueryWithPassThrough_CacheHit_ReturnsData() {
    // Arrange
    Shop shop = createTestShop(1L, "Test Shop");
    String key = "cache:shop:1";
    when(valueOperations.get(key)).thenReturn(JSONUtil.toJsonStr(shop));

    Function<Long, Shop> dbFallback = mock(Function.class);

    // Act
    Shop result = cacheClient.queryWithPassThrough("cache:shop:", 1L, Shop.class, dbFallback, 30L,
        TimeUnit.MINUTES);

    // Assert
    assertNotNull(result);
    assertEquals(1L, result.getId());
    verify(dbFallback, never()).apply(any());
  }

  /**
   * Test 4: queryWithPassThrough() - Cache miss queries database
   */
  @Test
  void testQueryWithPassThrough_CacheMiss_QueriesDatabase() {
    // Arrange
    String key = "cache:shop:1";
    Shop shop = createTestShop(1L, "Test Shop");

    when(valueOperations.get(key)).thenReturn(null);
    Function<Long, Shop> dbFallback = id -> shop;

    // Act
    Shop result = cacheClient.queryWithPassThrough("cache:shop:", 1L, Shop.class, dbFallback, 30L,
        TimeUnit.MINUTES);

    // Assert
    assertNotNull(result);
    verify(valueOperations).set(eq(key), anyString(), eq(30L), eq(TimeUnit.MINUTES));
  }

  /**
   * Test 5: queryWithPassThrough() - Null value caching prevents cache penetration
   */
  @Test
  void testQueryWithPassThrough_NullValue_CachesEmptyString() {
    // Arrange
    String key = "cache:shop:999";

    when(valueOperations.get(key)).thenReturn(null);
    Function<Long, Shop> dbFallback = id -> null; // Database returns null

    // Act
    Shop result = cacheClient.queryWithPassThrough("cache:shop:", 999L, Shop.class, dbFallback, 30L,
        TimeUnit.MINUTES);

    // Assert
    assertNull(result);

    // Verify empty string was cached (cache penetration protection)
    verify(valueOperations).set(
        eq(key),
        eq(""),
        eq(2L), // CACHE_NULL_TTL
        eq(TimeUnit.MINUTES)
    );
  }

  /**
   * Test 6: queryWithPassThrough() - Cached null value returns null immediately
   */
  @Test
  @SuppressWarnings("unchecked")
  void testQueryWithPassThrough_CachedNullValue_ReturnsNull() {
    // Arrange
    String key = "cache:shop:999";
    when(valueOperations.get(key)).thenReturn(""); // Cached empty string

    Function<Long, Shop> dbFallback = mock(Function.class);

    // Act
    Shop result = cacheClient.queryWithPassThrough("cache:shop:", 999L, Shop.class, dbFallback, 30L,
        TimeUnit.MINUTES);

    // Assert
    assertNull(result);
    verify(dbFallback, never()).apply(any()); // Database should NOT be called
  }

  /**
   * Test 7: queryWithLogicalExpire() - Cold start (empty cache) queries database
   */
  @Test
  void testQueryWithLogicalExpire_ColdStart_QueriesDatabase() {
    // Arrange
    String key = "cache:shop:1";
    Shop shop = createTestShop(1L, "Test Shop");

    when(valueOperations.get(key)).thenReturn(null); // Cache is empty
    Function<Long, Shop> dbFallback = id -> shop;

    // Act
    Shop result = cacheClient.queryWithLogicalExpire("cache:shop:", 1L, Shop.class, dbFallback, 30L,
        TimeUnit.MINUTES);

    // Assert
    assertNotNull(result);
    assertEquals(1L, result.getId());

    // Verify data was cached with logical expiration
    ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
    verify(valueOperations).set(eq(key), valueCaptor.capture());

    String cachedJson = valueCaptor.getValue();
    RedisData redisData = JSONUtil.toBean(cachedJson, RedisData.class);
    assertNotNull(redisData.getExpireTime());
  }

  /**
   * Test 8: queryWithLogicalExpire() - Valid cache returns data without database query
   */
  @Test
  @SuppressWarnings("unchecked")
  void testQueryWithLogicalExpire_ValidCache_ReturnsData() {
    // Arrange
    String key = "cache:shop:1";
    Shop shop = createTestShop(1L, "Test Shop");

    // Create RedisData with future expiration
    RedisData redisData = new RedisData();
    redisData.setData(shop);
    redisData.setExpireTime(LocalDateTime.now().plusMinutes(10)); // Not expired

    when(valueOperations.get(key)).thenReturn(JSONUtil.toJsonStr(redisData));
    Function<Long, Shop> dbFallback = mock(Function.class);

    // Act
    Shop result = cacheClient.queryWithLogicalExpire("cache:shop:", 1L, Shop.class, dbFallback, 30L,
        TimeUnit.MINUTES);

    // Assert
    assertNotNull(result);
    assertEquals(1L, result.getId());
    verify(dbFallback, never()).apply(any()); // Database should NOT be called
  }

  /**
   * Test 9: queryWithLogicalExpire() - Expired cache returns stale data immediately
   */
  @Test
  void testQueryWithLogicalExpire_ExpiredCache_ReturnsStaleData() throws InterruptedException {
    // Arrange
    String key = "cache:shop:1";
    Shop shop = createTestShop(1L, "Test Shop");

    // Create RedisData with past expiration
    RedisData redisData = new RedisData();
    redisData.setData(shop);
    redisData.setExpireTime(LocalDateTime.now().minusMinutes(1)); // Expired

    when(valueOperations.get(key)).thenReturn(JSONUtil.toJsonStr(redisData));
    when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
        .thenReturn(true); // Lock acquired

    Function<Long, Shop> dbFallback = id -> createTestShop(1L, "Fresh Shop");

    // Act
    Shop result = cacheClient.queryWithLogicalExpire("cache:shop:", 1L, Shop.class, dbFallback, 30L,
        TimeUnit.MINUTES);

    // Assert
    assertNotNull(result);
    assertEquals("Test Shop", result.getName()); // Returns STALE data immediately

    // Give async thread time to execute
    Thread.sleep(100);

    // Verify lock was acquired and released
    verify(valueOperations).setIfAbsent(eq("lock:cache:shop:1"), eq("1"), eq(10L),
        eq(TimeUnit.SECONDS));
    verify(stringRedisTemplate, timeout(200)).delete("lock:cache:shop:1");
  }

  /**
   * Test 10: set() - Stores data in Redis with TTL
   */
  @Test
  void testSet_StoresDataWithTTL() {
    // Arrange
    Shop shop = createTestShop(1L, "Test Shop");
    String key = "cache:shop:1";

    // Act
    cacheClient.set(key, shop, 30L, TimeUnit.MINUTES);

    // Assert
    verify(valueOperations).set(
        eq(key),
        eq(JSONUtil.toJsonStr(shop)),
        eq(30L),
        eq(TimeUnit.MINUTES)
    );
  }

  /**
   * Test 11: setLogicalExpire() - Stores data with logical expiration
   */
  @Test
  void testSetLogicalExpire_StoresWithExpiration() {
    Shop shop = createTestShop(1L, "Test Shop");
    String key = "cache:shop:1";

    // Act
    cacheClient.setLogicalExpire(key, shop, 30L, TimeUnit.MINUTES);

    // Assert
    ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
    verify(valueOperations).set(eq(key), valueCaptor.capture());

    String cachedJson = valueCaptor.getValue();
    RedisData redisData = JSONUtil.toBean(cachedJson, RedisData.class);

    assertNotNull(redisData.getExpireTime());
    assertTrue(redisData.getExpireTime().isAfter(LocalDateTime.now()));
    assertNotNull(redisData.getData());
  }

  // Helper method to create test Shop objects
  private Shop createTestShop(Long id, String name) {
    Shop shop = new Shop();
    shop.setId(id);
    shop.setName(name);
    shop.setTypeId(1L);
    shop.setArea("Downtown");
    shop.setAddress("123 Main St");
    return shop;
  }

  /**
   * Test 12: queryWithMutex() - Cache hit returns data without locking
   */
  @Test
  @SuppressWarnings("unchecked")
  void testQueryWithMutex_CacheHit_ReturnsData() {
    Shop shop = createTestShop(1L, "Test Shop");
    String key = "cache:shop:1";
    when(valueOperations.get(key)).thenReturn(JSONUtil.toJsonStr(shop));

    Function<Long, Shop> dbFallback = mock(Function.class);

    // Act
    Shop result = cacheClient.queryWithMutex("cache:shop:", 1L, Shop.class, dbFallback, 30L,
        TimeUnit.MINUTES);

    // Assert
    assertNotNull(result);
    assertEquals(1L, result.getId());
    verify(dbFallback, never()).apply(any()); // Database should NOT be called
    verify(valueOperations, never()).setIfAbsent(anyString(), anyString(), anyLong(),
        any(TimeUnit.class)); // No lock needed
  }

  /**
   * Test 13: queryWithMutex() - Cache miss with successful lock acquisition
   */
  @Test
  void testQueryWithMutex_CacheMiss_AcquiresLockAndQueriesDB() {
    String key = "cache:shop:1";
    String lockKey = "lock:cache:shop:1";
    Shop shop = createTestShop(1L, "Test Shop");

    // First call returns null (cache miss), second call returns null (double-check)
    when(valueOperations.get(key)).thenReturn((String) null, (String) null);
    when(valueOperations.setIfAbsent(lockKey, "1", 10L, TimeUnit.SECONDS))
        .thenReturn(true); // Successfully acquired lock

    Function<Long, Shop> dbFallback = id -> shop;

    // Act
    Shop result = cacheClient.queryWithMutex("cache:shop:", 1L, Shop.class, dbFallback, 30L,
        TimeUnit.MINUTES);

    // Assert
    assertNotNull(result);
    assertEquals(1L, result.getId());

    // Verify lock was acquired and released
    verify(valueOperations).setIfAbsent(lockKey, "1", 10L, TimeUnit.SECONDS);
    verify(stringRedisTemplate).delete(lockKey);

    // Verify data was cached
    verify(valueOperations).set(eq(key), anyString(), eq(30L), eq(TimeUnit.MINUTES));
  }

  /**
   * Test 14: queryWithMutex() - Failed lock acquisition triggers retry, then succeeds
   */
  @Test
  void testQueryWithMutex_FailedLock_RetriesThenSucceeds() {
    // Arrange
    String key = "cache:shop:1";
    String lockKey = "lock:cache:shop:1";
    Shop shop = createTestShop(1L, "Test Shop");

    // Mock responses: cache miss on both checks
    when(valueOperations.get(key)).thenReturn(null);

    // Mock lock behavior: fail first, succeed second
    when(valueOperations.setIfAbsent(lockKey, "1", 10L, TimeUnit.SECONDS))
        .thenReturn(false)  // First attempt: lock fails
        .thenReturn(true);   // Second attempt: lock succeeds

    Function<Long, Shop> dbFallback = id -> shop;

    // Act
    Shop result = cacheClient.queryWithMutex("cache:shop:", 1L, Shop.class, dbFallback, 30L,
        TimeUnit.MINUTES);

    // Assert
    assertNotNull(result);
    assertEquals(1L, result.getId());

    // Verify lock was attempted twice
    verify(valueOperations, times(2)).setIfAbsent(
        eq(lockKey),
        eq("1"),
        eq(10L),
        eq(TimeUnit.SECONDS)
    );

    // Verify data was cached
    verify(valueOperations).set(eq(key), anyString(), eq(30L), eq(TimeUnit.MINUTES));

    // Verify lock was released
    verify(stringRedisTemplate).delete(lockKey);
  }

  /**
   * Test 15: queryWithMutex() - Null value from database gets cached
   */
  @Test
  void testQueryWithMutex_NullValueFromDB_CachesEmpty() {
    // Arrange
    String key = "cache:shop:999";
    String lockKey = "lock:cache:shop:999";

    when(valueOperations.get(key)).thenReturn((String) null, (String) null);
    when(valueOperations.setIfAbsent(lockKey, "1", 10L, TimeUnit.SECONDS))
        .thenReturn(true); // Successfully gets lock

    Function<Long, Shop> dbFallback = id -> null; // Database returns null

    // Act
    Shop result = cacheClient.queryWithMutex("cache:shop:", 999L, Shop.class, dbFallback, 30L,
        TimeUnit.MINUTES);

    // Assert
    assertNull(result);

    // Verify empty string was cached to prevent cache penetration
    verify(valueOperations).set(
        eq(key),
        eq(""),
        eq(2L), // CACHE_NULL_TTL
        eq(TimeUnit.MINUTES)
    );

    // Verify lock was released
    verify(stringRedisTemplate).delete(lockKey);
  }

}