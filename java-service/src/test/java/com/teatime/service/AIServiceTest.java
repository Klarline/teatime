package com.teatime.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.teatime.config.AIServiceConfig;
import com.teatime.dto.ai.AIHealthResponse;
import com.teatime.dto.ai.RecommendationRequest;
import com.teatime.dto.ai.RecommendationResponse;
import com.teatime.dto.ai.ReviewDocument;
import com.teatime.service.impl.AIServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class AIServiceTest {

  @Mock
  private RestTemplate aiServiceRestTemplate;

  @Mock
  private AIServiceConfig aiServiceConfig;

  private AIServiceImpl aiService;

  @BeforeEach
  void setUp() {
    aiService = new AIServiceImpl();
    ReflectionTestUtils.setField(aiService, "aiServiceRestTemplate", aiServiceRestTemplate);
    ReflectionTestUtils.setField(aiService, "aiServiceConfig", aiServiceConfig);
    when(aiServiceConfig.getAiServiceUrl()).thenReturn("http://localhost:8000");
  }

  @Test
  void testGetRecommendations_Success() {
    RecommendationRequest request = new RecommendationRequest();
    request.setQuery("quiet cafe");
    request.setMaxResults(5);
    RecommendationResponse expected = new RecommendationResponse();
    expected.setRecommendations("Try Downtown Tea House");
    expected.setSourceBlogs(java.util.List.of(1, 2));

    when(aiServiceRestTemplate.postForObject(anyString(), eq(request), eq(RecommendationResponse.class)))
        .thenReturn(expected);

    RecommendationResponse result = aiService.getRecommendations(request);

    assertNotNull(result);
    assertEquals("Try Downtown Tea House", result.getRecommendations());
    assertEquals(2, result.getSourceBlogs().size());
  }

  @Test
  void testGetRecommendations_ServiceUnavailable_ReturnsFallback() {
    RecommendationRequest request = new RecommendationRequest();
    request.setQuery("quiet cafe");

    when(aiServiceRestTemplate.postForObject(anyString(), any(), eq(RecommendationResponse.class)))
        .thenThrow(new RestClientException("Connection refused"));

    RecommendationResponse result = aiService.getRecommendations(request);

    assertNotNull(result);
    assertTrue(result.getRecommendations().contains("AI service is currently unavailable"));
  }

  @Test
  void testIngestReview_Success() {
    ReviewDocument review = new ReviewDocument();
    review.setReviewId(1L);
    review.setShopId(1L);
    review.setContent("Great tea");
    review.setShopName("Test Shop");

    aiService.ingestReview(review);

    verify(aiServiceRestTemplate).postForObject(
        eq("http://localhost:8000/ai/ingest"),
        eq(review),
        eq(Void.class)
    );
  }

  @Test
  void testIngestReview_ServiceFails_DoesNotThrow() {
    ReviewDocument review = new ReviewDocument();
    review.setReviewId(1L);
    review.setShopId(1L);
    review.setContent("Great tea");
    review.setShopName("Test Shop");

    when(aiServiceRestTemplate.postForObject(anyString(), any(), eq(Void.class)))
        .thenThrow(new RestClientException("Connection refused"));

    assertDoesNotThrow(() -> aiService.ingestReview(review));
  }

  @Test
  void testCheckHealth_Success() {
    AIHealthResponse expected = new AIHealthResponse();
    expected.setStatus("healthy");
    expected.setVectorDbCount(150);

    when(aiServiceRestTemplate.getForObject(anyString(), eq(AIHealthResponse.class)))
        .thenReturn(expected);

    AIHealthResponse result = aiService.checkHealth();

    assertEquals("healthy", result.getStatus());
    assertEquals(150, result.getVectorDbCount());
  }

  @Test
  void testCheckHealth_ServiceUnavailable_ReturnsUnhealthy() {
    when(aiServiceRestTemplate.getForObject(anyString(), eq(AIHealthResponse.class)))
        .thenThrow(new RestClientException("Connection refused"));

    AIHealthResponse result = aiService.checkHealth();

    assertEquals("unhealthy", result.getStatus());
    assertEquals(0, result.getVectorDbCount());
  }

  @Test
  void testIsServiceHealthy_WhenHealthy_ReturnsTrue() {
    AIHealthResponse health = new AIHealthResponse();
    health.setStatus("healthy");
    when(aiServiceRestTemplate.getForObject(anyString(), eq(AIHealthResponse.class)))
        .thenReturn(health);

    boolean result = aiService.isServiceHealthy();

    assertTrue(result);
  }

  @Test
  void testIsServiceHealthy_WhenUnhealthy_ReturnsFalse() {
    AIHealthResponse health = new AIHealthResponse();
    health.setStatus("unhealthy");
    when(aiServiceRestTemplate.getForObject(anyString(), eq(AIHealthResponse.class)))
        .thenReturn(health);

    boolean result = aiService.isServiceHealthy();

    assertFalse(result);
  }
}
