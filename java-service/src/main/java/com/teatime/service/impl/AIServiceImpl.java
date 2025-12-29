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

  @Override
  public void ingestReview(ReviewDocument review) {
    try {
      String url = aiServiceConfig.getAiServiceUrl() + "/ai/ingest";
      aiServiceRestTemplate.postForObject(url, review, Void.class);
    } catch (RestClientException e) {
      log.error("Failed to ingest review into AI service", e);
    }
  }

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

  @Override
  public boolean isServiceHealthy() {
    AIHealthResponse health = checkHealth();
    return "healthy".equals(health.getStatus());
  }
}