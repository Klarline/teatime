package com.teatime.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * BlogCommentsController has no endpoints yet; tests that the controller is loadable.
 */
class BlogCommentsControllerTest {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new BlogCommentsController()).build();
  }

  @Test
  void testControllerBasePath_Resolves() throws Exception {
    // Controller has no endpoints; request to base path returns 404 (no handler)
    mockMvc.perform(get("/api/blog-comments"))
        .andExpect(status().isNotFound());
  }
}
