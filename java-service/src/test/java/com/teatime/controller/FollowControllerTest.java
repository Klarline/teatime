package com.teatime.controller;

import com.teatime.dto.Result;
import com.teatime.service.IFollowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FollowControllerTest {

  private MockMvc mockMvc;

  @Mock
  private IFollowService followService;

  @InjectMocks
  private FollowController followController;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(followController).build();
  }

  /**
   * Test 1: PUT /api/follow/{id}/true - Follow a user
   */
  @Test
  void testFollowUser_Follow() throws Exception {
    // Arrange
    Long followUserId = 2L;
    when(followService.followUser(eq(followUserId), eq(true))).thenReturn(Result.ok());

    // Act & Assert
    mockMvc.perform(put("/api/follow/{id}/{isFollow}", followUserId, true))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  /**
   * Test 2: PUT /api/follow/{id}/false - Unfollow a user
   */
  @Test
  void testFollowUser_Unfollow() throws Exception {
    // Arrange
    Long followUserId = 2L;
    when(followService.followUser(eq(followUserId), eq(false))).thenReturn(Result.ok());

    // Act & Assert
    mockMvc.perform(put("/api/follow/{id}/{isFollow}", followUserId, false))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  /**
   * Test 3: GET /api/follow/or/not/{id} - Check if following
   */
  @Test
  void testIsFollowed_True() throws Exception {
    // Arrange
    Long followUserId = 2L;
    when(followService.isFollowed(followUserId)).thenReturn(Result.ok(true));

    // Act & Assert
    mockMvc.perform(get("/api/follow/or/not/{id}", followUserId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").value(true));
  }

  /**
   * Test 4: GET /api/follow/common/{id} - Get common followers
   */
  @Test
  void testCommonFollow_Success() throws Exception {
    // Arrange
    Long userId = 2L;
    when(followService.commonFollow(userId)).thenReturn(Result.ok(Arrays.asList()));

    // Act & Assert
    mockMvc.perform(get("/api/follow/common/{id}", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  /**
   * Test 5: GET /api/follow/followers/count/{id} - Get follower count
   */
  @Test
  void testGetFollowerCount_Success() throws Exception {
    // Arrange
    Long userId = 2L;
    when(followService.getFollowerCount(userId)).thenReturn(Result.ok(10L));

    // Act & Assert
    mockMvc.perform(get("/api/follow/followers/count/{id}", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").value(10));
  }

  /**
   * Test 6: GET /api/follow/following/count/{id} - Get following count
   */
  @Test
  void testGetFollowingCount_Success() throws Exception {
    // Arrange
    Long userId = 2L;
    when(followService.getFollowingCount(userId)).thenReturn(Result.ok(5L));

    // Act & Assert
    mockMvc.perform(get("/api/follow/following/count/{id}", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").value(5));
  }
}