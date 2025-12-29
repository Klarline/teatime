package com.teatime.dto.ai;

import lombok.Data;

@Data
public class RecommendationRequest {
  private String query;
  private Integer maxResults = 5;
}