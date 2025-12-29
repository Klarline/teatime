package com.teatime.dto.ai;

import lombok.Data;
import java.util.List;

@Data
public class RecommendationResponse {
  private String recommendations;
  private List<Integer> sourceBlogs;
}