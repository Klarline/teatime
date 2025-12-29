package com.teatime.dto.ai;

import lombok.Data;

@Data
public class AIHealthResponse {
  private String status;
  private Integer vectorDbCount;
}