package com.teatime.controller;

import com.teatime.dto.Result;
import com.teatime.dto.ai.*;
import com.teatime.service.IAIService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/ai")
public class AIController {

  @Resource
  private IAIService aiService;

  /**
   * Endpoint to get tea shop recommendations based on user preferences.
   * POST /api/ai/recommend
   */
  @PostMapping("/recommend")
  public Result getRecommendations(@RequestBody RecommendationRequest request) {
    RecommendationResponse response = aiService.getRecommendations(request);
    return Result.ok(response);
  }

  /**
   * Endpoint to check the health status of the AI service with vector DB count
   * GET /api/ai/health
   */
  @GetMapping("/health")
  public Result checkHealth() {
    AIHealthResponse health = aiService.checkHealth();
    return Result.ok(health);
  }
}