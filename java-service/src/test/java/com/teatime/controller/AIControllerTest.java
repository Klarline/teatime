package com.teatime.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teatime.dto.ai.AIHealthResponse;
import com.teatime.dto.ai.RecommendationRequest;
import com.teatime.dto.ai.RecommendationResponse;
import com.teatime.service.IAIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AIControllerTest {

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @Mock
  private IAIService aiService;

  @InjectMocks
  private AIController aiController;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(aiController).build();
    objectMapper = new ObjectMapper();
  }

  /**
   * Test 1: POST /api/ai/recommend - Get recommendations
   */
  @Test
  void testGetRecommendations_Success() throws Exception {
    // Arrange
    RecommendationRequest request = new RecommendationRequest();
    request.setQuery("quiet cafe for studying");
    request.setMaxResults(5);

    RecommendationResponse response = new RecommendationResponse();
    response.setRecommendations("Here are some quiet cafes...");
    response.setSourceBlogs(Arrays.asList(1, 2, 3));

    when(aiService.getRecommendations(any(RecommendationRequest.class))).thenReturn(response);

    // Act & Assert
    mockMvc.perform(post("/api/ai/recommend")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.recommendations").exists());
  }

  /**
   * Test 2: GET /api/ai/health - Check AI service health
   */
  @Test
  void testCheckHealth_Success() throws Exception {
    // Arrange
    AIHealthResponse health = new AIHealthResponse();
    health.setStatus("healthy");
    health.setVectorDbCount(150);

    when(aiService.checkHealth()).thenReturn(health);

    // Act & Assert
    mockMvc.perform(get("/api/ai/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.status").value("healthy"))
        .andExpect(jsonPath("$.data.vectorDbCount").value(150));
  }
}