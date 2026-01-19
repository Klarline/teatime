package com.teatime.controller;

import com.teatime.dto.Result;
import com.teatime.service.IShopTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ShopTypeControllerTest {

  private MockMvc mockMvc;

  @Mock
  private IShopTypeService typeService;

  @InjectMocks
  private ShopTypeController shopTypeController;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(shopTypeController).build();
  }

  /**
   * Test 1: GET /api/shop-type/list - Get all shop types
   */
  @Test
  void testQueryTypeList_Success() throws Exception {
    // Arrange
    when(typeService.queryTypeList()).thenReturn(Result.ok(Arrays.asList()));

    // Act & Assert
    mockMvc.perform(get("/api/shop-type/list"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }
}