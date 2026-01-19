package com.teatime.service.impl;

import com.teatime.config.AIServiceConfig;
import com.teatime.dto.ai.*;
import com.teatime.service.IAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import javax.annotation.Resource;

@Slf4j
@Service
public class AIServiceImpl implements IAIService {

  @Resource
  private RestTemplate aiServiceRestTemplate;

  @Resource
  private AIServiceConfig aiServiceConfig;

  /**
   * Communicates with the external AI service to get tea shop recommendations
   * based on user preferences.
   *
   * @param request The recommendation request containing user preferences.
   * @return The recommendation response from the AI service.
   */
  @Override
  public RecommendationResponse getRecommendations(RecommendationRequest request) {
    try {
      String url = aiServiceConfig.getAiServiceUrl() + "/ai/recommend";
      return aiServiceRestTemplate.postForObject(url, request, RecommendationResponse.class);
    } catch (RestClientException e) {
      log.error("Failed to get recommendations from AI service", e);
      // Return fallback response
      RecommendationResponse fallback = new RecommendationResponse();
      fallback.setRecommendations(
          "AI service is currently unavailable. Please try browsing our tea shops manually.");
      return fallback;
    }
  }

  /**
   * Ingests a review document into the AI service for future analysis.
   *
   * @param review The review document to be ingested.
   */
  @Override
  public void ingestReview(ReviewDocument review) {
    try {
      String url = aiServiceConfig.getAiServiceUrl() + "/ai/ingest";
      aiServiceRestTemplate.postForObject(url, review, Void.class);
    } catch (RestClientException e) {
      log.error("Failed to ingest review into AI service", e);
    }
  }

  /**
   * Checks the health status of the AI service.
   *
   * @return The health response from the AI service.
   */
  @Override
  public AIHealthResponse checkHealth() {
    try {
      String url = aiServiceConfig.getAiServiceUrl() + "/ai/health";
      return aiServiceRestTemplate.getForObject(url, AIHealthResponse.class);
    } catch (RestClientException e) {
      AIHealthResponse response = new AIHealthResponse();
      response.setStatus("unhealthy");
      response.setVectorDbCount(0);
      return response;
    }
  }

  /**
   * Determines if the AI service is healthy based on its health status.
   *
   * @return true if the AI service is healthy, false otherwise.
   */
  @Override
  public boolean isServiceHealthy() {
    AIHealthResponse health = checkHealth();
    return "healthy".equals(health.getStatus());
  }
}