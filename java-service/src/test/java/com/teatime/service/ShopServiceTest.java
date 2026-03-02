package com.teatime.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.teatime.dto.Result;
import com.teatime.entity.Shop;
import com.teatime.repository.ShopRepository;
import com.teatime.service.impl.ShopServiceImpl;
import com.teatime.utils.CacheClient;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

  @Mock
  private ShopRepository shopRepository;

  @Mock
  private StringRedisTemplate stringRedisTemplate;

  @Mock
  private CacheClient cacheClient;

  @InjectMocks
  private ShopServiceImpl shopService;

  @Test
  void testQueryById_CacheHit_ReturnsShop() {
    Long id = 1L;
    Shop shop = createTestShop(id, "Test Shop");
    when(cacheClient.queryWithLogicalExpire(anyString(), eq(id), eq(Shop.class), any(), anyLong(), any()))
        .thenReturn(shop);

    Result result = shopService.queryById(id);

    assertTrue(result.getSuccess());
    assertEquals(shop, result.getData());
  }

  @Test
  void testQueryById_NotFound_ReturnsFail() {
    Long id = 999L;
    when(cacheClient.queryWithLogicalExpire(anyString(), eq(id), eq(Shop.class), any(), anyLong(), any()))
        .thenReturn(null);

    Result result = shopService.queryById(id);

    assertFalse(result.getSuccess());
    assertEquals("Shop does not exist!", result.getErrorMsg());
  }

  @Test
  void testUpdate_Success() {
    Shop shop = createTestShop(1L, "Updated Shop");
    when(shopRepository.save(any(Shop.class))).thenReturn(shop);

    Result result = shopService.update(shop);

    assertTrue(result.getSuccess());
    verify(shopRepository).save(shop);
    verify(stringRedisTemplate).delete("teatime:cache:shop:1");
  }

  @Test
  void testUpdate_NullId_ReturnsFail() {
    Shop shop = createTestShop(null, "No ID");

    Result result = shopService.update(shop);

    assertFalse(result.getSuccess());
    assertEquals("Shop id cannot be null!", result.getErrorMsg());
    verify(shopRepository, never()).save(any());
  }

  @Test
  void testQueryShopByType_WithoutCoordinates_ReturnsShops() {
    List<Shop> shops = Arrays.asList(
        createTestShop(1L, "Shop 1"),
        createTestShop(2L, "Shop 2")
    );
    when(shopRepository.findByTypeId(eq(1L), any()))
        .thenReturn(new org.springframework.data.domain.PageImpl<>(shops));

    Result result = shopService.queryShopByType(1, 1, null, null);

    assertTrue(result.getSuccess());
    @SuppressWarnings("unchecked")
    List<Shop> resultList = (List<Shop>) result.getData();
    assertEquals(2, resultList.size());
  }

  @Test
  void testQueryShopByName_WithName_ReturnsMatchingShops() {
    List<Shop> shops = List.of(createTestShop(1L, "Tea House"));
    when(shopRepository.findByNameContaining(eq("Tea"), any()))
        .thenReturn(new org.springframework.data.domain.PageImpl<>(shops));

    Result result = shopService.queryShopByName("Tea", 1);

    assertTrue(result.getSuccess());
    @SuppressWarnings("unchecked")
    List<Shop> resultList = (List<Shop>) result.getData();
    assertEquals(1, resultList.size());
    assertEquals("Tea House", resultList.get(0).getName());
  }

  @Test
  void testQueryShopByName_BlankName_ReturnsAllShops() {
    List<Shop> shops = Arrays.asList(
        createTestShop(1L, "Shop A"),
        createTestShop(2L, "Shop B")
    );
    when(shopRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
        .thenReturn(new org.springframework.data.domain.PageImpl<>(shops));

    Result result = shopService.queryShopByName("", 1);

    assertTrue(result.getSuccess());
    @SuppressWarnings("unchecked")
    List<Shop> resultList = (List<Shop>) result.getData();
    assertEquals(2, resultList.size());
  }

  @Test
  void testGetById_Exists_ReturnsShop() {
    Long id = 1L;
    Shop shop = createTestShop(id, "Test");
    when(shopRepository.findById(id)).thenReturn(Optional.of(shop));

    Shop result = shopService.getById(id);

    assertNotNull(result);
    assertEquals(id, result.getId());
  }

  @Test
  void testGetById_NotExists_ReturnsNull() {
    when(shopRepository.findById(999L)).thenReturn(Optional.empty());

    Shop result = shopService.getById(999L);

    assertNull(result);
  }

  @Test
  void testSave_CallsRepository() {
    Shop shop = createTestShop(null, "New Shop");
    when(shopRepository.save(any(Shop.class))).thenReturn(shop);

    shopService.save(shop);

    verify(shopRepository).save(shop);
  }

  @Test
  void testList_ReturnsAllShops() {
    List<Shop> shops = List.of(
        createTestShop(1L, "Shop 1"),
        createTestShop(2L, "Shop 2")
    );
    when(shopRepository.findAll()).thenReturn(shops);

    List<Shop> result = shopService.list();

    assertEquals(2, result.size());
    verify(shopRepository).findAll();
  }

  @Test
  void testQueryShopByName_NullName_ReturnsAllShops() {
    List<Shop> shops = List.of(createTestShop(1L, "Shop A"));
    when(shopRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
        .thenReturn(new org.springframework.data.domain.PageImpl<>(shops));

    Result result = shopService.queryShopByName(null, 1);

    assertTrue(result.getSuccess());
    @SuppressWarnings("unchecked")
    List<Shop> resultList = (List<Shop>) result.getData();
    assertEquals(1, resultList.size());
  }

  private Shop createTestShop(Long id, String name) {
    Shop shop = new Shop();
    shop.setId(id);
    shop.setName(name);
    shop.setTypeId(1L);
    shop.setArea("Downtown");
    shop.setAddress("123 Main St");
    return shop;
  }
}
