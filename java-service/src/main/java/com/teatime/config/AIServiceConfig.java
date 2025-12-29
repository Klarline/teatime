package com.teatime.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AIServiceConfig {

  @Value("${teatime.ai.service.url:http://localhost:8000}")
  private String aiServiceUrl;

  @Value("${teatime.ai.service.timeout:30000}")
  private int timeout;

  @Bean
  public RestTemplate aiServiceRestTemplate() {
    return new RestTemplate();
  }

  public String getAiServiceUrl() {
    return aiServiceUrl;
  }

  public int getTimeout() {
    return timeout;
  }
}