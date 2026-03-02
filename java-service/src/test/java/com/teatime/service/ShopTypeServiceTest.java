package com.teatime.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.teatime.dto.Result;
import com.teatime.entity.ShopType;
import com.teatime.repository.ShopTypeRepository;
import com.teatime.service.impl.ShopTypeServiceImpl;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ShopTypeServiceTest {

  @Mock
  private ShopTypeRepository shopTypeRepository;

  @Mock
  private StringRedisTemplate stringRedisTemplate;

  @Mock
  private ValueOperations<String, String> valueOperations;

  private ShopTypeServiceImpl shopTypeService;

  @BeforeEach
  void setUp() {
    shopTypeService = new ShopTypeServiceImpl();
    ReflectionTestUtils.setField(shopTypeService, "shopTypeRepository", shopTypeRepository);
    ReflectionTestUtils.setField(shopTypeService, "stringRedisTemplate", stringRedisTemplate);
    lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
  }

  @Test
  void testQueryTypeList_CacheHit_ReturnsFromCache() {
    ShopType type = new ShopType();
    type.setId(1L);
    type.setName("Bubble Tea");
    String cachedJson = "[{\"id\":1,\"name\":\"Bubble Tea\"}]";
    when(valueOperations.get(anyString())).thenReturn(cachedJson);

    Result result = shopTypeService.queryTypeList();

    assertTrue(result.getSuccess());
    @SuppressWarnings("unchecked")
    List<ShopType> types = (List<ShopType>) result.getData();
    assertEquals(1, types.size());
    verify(shopTypeRepository, never()).findAll(any(Sort.class));
  }

  @Test
  void testQueryTypeList_CacheMiss_ReturnsFromDbAndCaches() {
    ShopType type = new ShopType();
    type.setId(1L);
    type.setName("Bubble Tea");
    when(valueOperations.get(anyString())).thenReturn(null);
    when(shopTypeRepository.findAll(any(Sort.class))).thenReturn(List.of(type));

    Result result = shopTypeService.queryTypeList();

    assertTrue(result.getSuccess());
    @SuppressWarnings("unchecked")
    List<ShopType> types = (List<ShopType>) result.getData();
    assertEquals(1, types.size());
    verify(valueOperations).set(anyString(), anyString());
  }
}
