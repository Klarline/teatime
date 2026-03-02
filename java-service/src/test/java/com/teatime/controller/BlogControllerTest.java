package com.teatime.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teatime.dto.Result;
import com.teatime.dto.UserDTO;
import com.teatime.entity.Blog;
import com.teatime.service.IBlogService;
import com.teatime.utils.UserHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BlogControllerTest {

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @Mock
  private IBlogService blogService;

  @InjectMocks
  private BlogController blogController;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(blogController).build();
    objectMapper = new ObjectMapper();
  }

  /**
   * Test 1: POST /api/blog - Create blog successfully
   */
  @Test
  void testSaveBlog_Success() throws Exception {
    // Arrange
    Blog blog = new Blog();
    blog.setContent("Great tea experience!");
    blog.setShopId(1L);

    when(blogService.saveBlog(any(Blog.class))).thenReturn(Result.ok(1L));

    // Act & Assert
    mockMvc.perform(post("/api/blog")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(blog)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").value(1L));
  }

  /**
   * Test 2: PUT /api/blog/like/{id} - Like a blog
   */
  @Test
  void testLikeBlog_Success() throws Exception {
    // Arrange
    Long blogId = 1L;
    when(blogService.likeBlog(blogId)).thenReturn(Result.ok());

    // Act & Assert
    mockMvc.perform(put("/api/blog/like/{id}", blogId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  /**
   * Test 3: GET /api/blog/hot - Get hot blogs
   */
  @Test
  void testQueryHotBlog_Success() throws Exception {
    // Arrange
    List<Blog> blogs = Arrays.asList(new Blog(), new Blog());
    when(blogService.queryHotBlog(1)).thenReturn(Result.ok(blogs));

    // Act & Assert
    mockMvc.perform(get("/api/blog/hot")
            .param("current", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.length()").value(2));
  }

  /**
   * Test 4: GET /api/blog/{id} - Get blog by ID
   */
  @Test
  void testQueryBlogById_Success() throws Exception {
    // Arrange
    Long blogId = 1L;
    Blog blog = new Blog();
    blog.setId(blogId);
    blog.setContent("Test content");

    when(blogService.queryBlogById(blogId)).thenReturn(Result.ok(blog));

    // Act & Assert
    mockMvc.perform(get("/api/blog/{id}", blogId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(blogId));
  }

  /**
   * Test 5: GET /api/blog/likes/{id} - Get users who liked a blog
   */
  @Test
  void testQueryBlogLikes_Success() throws Exception {
    // Arrange
    Long blogId = 1L;
    when(blogService.queryBlogLikes(blogId)).thenReturn(Result.ok(Arrays.asList()));

    // Act & Assert
    mockMvc.perform(get("/api/blog/likes/{id}", blogId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  /**
   * Test 6: GET /api/blog/of/me - Query my blogs
   */
  @Test
  void testQueryMyBlog_Success() throws Exception {
    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      UserDTO user = new UserDTO();
      user.setId(1L);
      userHolderMock.when(UserHolder::getUser).thenReturn(user);

      List<Blog> blogs = Arrays.asList(new Blog(), new Blog());
      when(blogService.queryBlogsByUserId(1L, 1)).thenReturn(blogs);

      mockMvc.perform(get("/api/blog/of/me").param("current", "1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.length()").value(2));
    }
  }

  /**
   * Test 7: GET /api/blog - Query blogs by user ID
   */
  @Test
  void testQueryBlogByUserId_Success() throws Exception {
    List<Blog> blogs = Arrays.asList(new Blog(), new Blog());
    when(blogService.queryBlogsByUserId(2L, 1)).thenReturn(blogs);

    mockMvc.perform(get("/api/blog")
            .param("id", "2")
            .param("current", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.length()").value(2));
  }

  /**
   * Test 8: GET /api/blog - Query blogs by user ID with default current
   */
  @Test
  void testQueryBlogByUserId_DefaultCurrent() throws Exception {
    List<Blog> blogs = Arrays.asList(new Blog());
    when(blogService.queryBlogsByUserId(3L, 1)).thenReturn(blogs);

    mockMvc.perform(get("/api/blog").param("id", "3"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.length()").value(1));
  }
}