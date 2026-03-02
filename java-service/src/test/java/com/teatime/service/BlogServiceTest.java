package com.teatime.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.teatime.dto.Result;
import com.teatime.dto.UserDTO;
import com.teatime.entity.Blog;
import com.teatime.entity.Follow;
import com.teatime.entity.User;
import com.teatime.repository.BlogRepository;
import com.teatime.service.impl.BlogServiceImpl;
import com.teatime.utils.UserHolder;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

@ExtendWith(MockitoExtension.class)
class BlogServiceTest {

  @Mock
  private BlogRepository blogRepository;

  @Mock
  private IUserService userService;

  @Mock
  private StringRedisTemplate stringRedisTemplate;

  @Mock
  private IFollowService followService;

  @Mock
  private IShopService shopService;

  @Mock
  private IAIService aiService;

  @Mock
  private ZSetOperations<String, String> zSetOperations;

  private BlogServiceImpl blogService;

  @BeforeEach
  void setUp() {
    lenient().when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
    blogService = new BlogServiceImpl(
        blogRepository, userService, stringRedisTemplate, followService);
    ReflectionTestUtils.setField(blogService, "shopService", shopService);
    ReflectionTestUtils.setField(blogService, "aiService", aiService);
  }

  @Test
  void testQueryHotBlog_ReturnsBlogs() {
    Blog blog = new Blog();
    blog.setId(1L);
    blog.setUserId(1L);
    blog.setContent("Test");
    when(blogRepository.findAllByOrderByLikedDesc(any(PageRequest.class)))
        .thenReturn(new PageImpl<>(List.of(blog)));
    when(userService.getById(1L)).thenReturn(createUser(1L, "TestUser"));

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(null);

      Result result = blogService.queryHotBlog(1);

      assertTrue(result.getSuccess());
      @SuppressWarnings("unchecked")
      List<Blog> blogs = (List<Blog>) result.getData();
      assertEquals(1, blogs.size());
    }
  }

  @Test
  void testQueryBlogById_WhenExists_ReturnsBlog() {
    Blog blog = new Blog();
    blog.setId(1L);
    blog.setUserId(1L);
    when(blogRepository.findById(1L)).thenReturn(java.util.Optional.of(blog));
    when(userService.getById(1L)).thenReturn(createUser(1L, "Author"));

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(null);

      Result result = blogService.queryBlogById(1L);

      assertTrue(result.getSuccess());
      assertEquals(blog, result.getData());
    }
  }

  @Test
  void testQueryBlogById_WhenNotExists_ReturnsFail() {
    when(blogRepository.findById(999L)).thenReturn(java.util.Optional.empty());

    Result result = blogService.queryBlogById(999L);

    assertFalse(result.getSuccess());
    assertEquals("Blog does not exist", result.getErrorMsg());
  }

  @Test
  void testLikeBlog_FirstLike_ReturnsOk() {
    when(zSetOperations.score(anyString(), anyString())).thenReturn(null);
    when(blogRepository.incrementLiked(1L)).thenReturn(1);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      UserDTO user = new UserDTO();
      user.setId(1L);
      userHolderMock.when(UserHolder::getUser).thenReturn(user);

      Result result = blogService.likeBlog(1L);

      assertTrue(result.getSuccess());
      verify(zSetOperations).add(anyString(), eq("1"), anyDouble());
    }
  }

  @Test
  void testLikeBlog_AlreadyLiked_Unlikes() {
    when(zSetOperations.score(anyString(), anyString())).thenReturn(1.0);
    when(blogRepository.decrementLiked(1L)).thenReturn(1);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      UserDTO user = new UserDTO();
      user.setId(1L);
      userHolderMock.when(UserHolder::getUser).thenReturn(user);

      Result result = blogService.likeBlog(1L);

      assertNull(result);
      verify(zSetOperations).remove(anyString(), eq("1"));
    }
  }

  @Test
  void testLikeBlog_FirstLike_IncrementReturnsZero_DoesNotAddToRedis() {
    when(zSetOperations.score(anyString(), anyString())).thenReturn(null);
    when(blogRepository.incrementLiked(1L)).thenReturn(0);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      UserDTO user = new UserDTO();
      user.setId(1L);
      userHolderMock.when(UserHolder::getUser).thenReturn(user);

      Result result = blogService.likeBlog(1L);

      assertTrue(result.getSuccess());
      verify(zSetOperations, never()).add(anyString(), anyString(), anyDouble());
    }
  }

  @Test
  void testLikeBlog_AlreadyLiked_DecrementReturnsZero_DoesNotRemoveFromRedis() {
    when(zSetOperations.score(anyString(), anyString())).thenReturn(1.0);
    when(blogRepository.decrementLiked(1L)).thenReturn(0);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      UserDTO user = new UserDTO();
      user.setId(1L);
      userHolderMock.when(UserHolder::getUser).thenReturn(user);

      Result result = blogService.likeBlog(1L);

      assertNull(result);
      verify(zSetOperations, never()).remove(anyString(), anyString());
    }
  }

  @Test
  void testQueryBlogById_WithUserLiked_SetsIsLike() {
    Blog blog = new Blog();
    blog.setId(1L);
    blog.setUserId(1L);
    when(blogRepository.findById(1L)).thenReturn(java.util.Optional.of(blog));
    when(userService.getById(1L)).thenReturn(createUser(1L, "Author"));
    when(zSetOperations.score(anyString(), eq("1"))).thenReturn(1.0);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      UserDTO user = new UserDTO();
      user.setId(1L);
      userHolderMock.when(UserHolder::getUser).thenReturn(user);

      Result result = blogService.queryBlogById(1L);

      assertTrue(result.getSuccess());
      Blog resultBlog = (Blog) result.getData();
      assertTrue(resultBlog.getIsLike());
    }
  }

  @Test
  void testSaveBlog_WithFollowers_AddsToFeed() {
    Blog blog = new Blog();
    blog.setShopId(1L);
    blog.setContent("Great tea!");
    Follow follow = new Follow();
    follow.setUserId(10L);
    follow.setFollowUserId(1L);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      UserDTO userDTO = new UserDTO();
      userDTO.setId(1L);
      userDTO.setNickName("Author");
      userHolderMock.when(UserHolder::getUser).thenReturn(userDTO);

      when(blogRepository.save(any(Blog.class))).thenAnswer(inv -> {
        Blog b = inv.getArgument(0);
        b.setId(1L);
        return b;
      });
      when(followService.findByFollowUserId(1L)).thenReturn(List.of(follow));
      lenient().when(shopService.getById(1L)).thenReturn(new com.teatime.entity.Shop());

      Result result = blogService.saveBlog(blog);

      assertTrue(result.getSuccess());
      verify(zSetOperations).add(contains("teatime:feed:10"), eq("1"), anyDouble());
    }
  }

  @Test
  void testQueryBlogLikes_NullTop5_ReturnsEmptyList() {
    when(zSetOperations.range(anyString(), eq(0L), eq(4L))).thenReturn(null);

    Result result = blogService.queryBlogLikes(1L);

    assertTrue(result.getSuccess());
    assertEquals(Collections.emptyList(), result.getData());
  }

  @Test
  void testQueryBlogLikes_Empty_ReturnsEmptyList() {
    when(zSetOperations.range(anyString(), eq(0L), eq(4L))).thenReturn(Collections.emptySet());

    Result result = blogService.queryBlogLikes(1L);

    assertTrue(result.getSuccess());
    assertEquals(Collections.emptyList(), result.getData());
  }

  @Test
  void testQueryBlogLikes_WithLikes_ReturnsUsers() {
    Set<String> userIds = Set.of("1", "2");
    when(zSetOperations.range(anyString(), eq(0L), eq(4L))).thenReturn(userIds);
    User user1 = createUser(1L, "User1");
    User user2 = createUser(2L, "User2");
    lenient().when(userService.listByIds(anyList())).thenReturn(new java.util.ArrayList<>(List.of(user1, user2)));

    Result result = blogService.queryBlogLikes(1L);

    assertTrue(result.getSuccess());
    List<?> users = (List<?>) result.getData();
    assertEquals(2, users.size());
  }

  @Test
  void testGetById_Exists_ReturnsBlog() {
    Blog blog = new Blog();
    blog.setId(1L);
    when(blogRepository.findById(1L)).thenReturn(java.util.Optional.of(blog));

    Blog result = blogService.getById(1L);

    assertNotNull(result);
    assertEquals(1L, result.getId());
  }

  @Test
  void testGetById_NotExists_ReturnsNull() {
    when(blogRepository.findById(999L)).thenReturn(java.util.Optional.empty());

    Blog result = blogService.getById(999L);

    assertNull(result);
  }

  @Test
  void testQueryBlogsByUserId_ReturnsBlogs() {
    Blog blog = new Blog();
    blog.setId(1L);
    blog.setUserId(1L);
    when(blogRepository.findByUserId(eq(1L), any(PageRequest.class)))
        .thenReturn(new PageImpl<>(List.of(blog)));

    List<Blog> result = blogService.queryBlogsByUserId(1L, 1);

    assertEquals(1, result.size());
    assertEquals(1L, result.get(0).getId());
  }

  @Test
  void testSaveBlog_Success() {
    Blog blog = new Blog();
    blog.setShopId(1L);
    blog.setContent("Great tea!");

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      com.teatime.dto.UserDTO userDTO = new com.teatime.dto.UserDTO();
      userDTO.setId(1L);
      userDTO.setNickName("Author");
      userHolderMock.when(UserHolder::getUser).thenReturn(userDTO);

      when(blogRepository.save(any(Blog.class))).thenAnswer(inv -> {
        Blog b = inv.getArgument(0);
        b.setId(1L);
        return b;
      });
      when(followService.findByFollowUserId(1L)).thenReturn(Collections.emptyList());
      lenient().when(shopService.getById(1L)).thenReturn(new com.teatime.entity.Shop());

      Result result = blogService.saveBlog(blog);

      assertTrue(result.getSuccess());
      assertEquals(1L, result.getData());
      verify(blogRepository).save(any(Blog.class));
    }
  }

  @Test
  void testSaveBlog_SaveReturnsNull_ReturnsFail() {
    Blog blog = new Blog();
    blog.setShopId(1L);
    blog.setContent("Great tea!");

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      com.teatime.dto.UserDTO userDTO = new com.teatime.dto.UserDTO();
      userDTO.setId(1L);
      userHolderMock.when(UserHolder::getUser).thenReturn(userDTO);

      when(blogRepository.save(any(Blog.class))).thenReturn(null);

      Result result = blogService.saveBlog(blog);

      assertFalse(result.getSuccess());
      assertEquals("Failed to publish blog", result.getErrorMsg());
    }
  }

  private User createUser(Long id, String nickName) {
    User user = new User();
    user.setId(id);
    user.setNickName(nickName);
    return user;
  }
}
