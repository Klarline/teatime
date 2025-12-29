package com.teatime.service;

import com.teatime.dto.ai.RecommendationRequest;
import com.teatime.dto.ai.RecommendationResponse;
import com.teatime.dto.ai.ReviewDocument;
import com.teatime.dto.ai.AIHealthResponse;

public interface IAIService {

  /**
   * Get tea shop recommendations based on user preferences.
   *
   * @param request Recommendation request
   * @return Recommendation response
   */
  RecommendationResponse getRecommendations(RecommendationRequest request);

  /**
   * Ingest a review document into the AI service for training.
   *
   * @param review Review document to ingest
   */
  void ingestReview(ReviewDocument review);

  /**
   * Check the health status of the AI service.
   *
   * @return AI health response
   */
  AIHealthResponse checkHealth();

  /**
   * Determine if the AI service is healthy.
   *
   * @return if is healthy
   */
  boolean isServiceHealthy();
}