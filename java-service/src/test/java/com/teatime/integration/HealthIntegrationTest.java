package com.teatime.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration test for application health and basic context loading.
 * Requires Redis to be running locally for full context startup.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Disabled("Requires Redis - enable when running: docker run -d -p 6379:6379 redis:7-alpine")
class HealthIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void testActuatorHealth_Returns200() throws Exception {
    mockMvc.perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").exists());
  }

  @Test
  void testApplicationContext_LoadsSuccessfully() {
    // If we get here, the Spring context loaded successfully
    // (no Redis, DB, or other beans failed to initialize)
  }
}
