package com.teatime.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.teatime.dto.Result;
import com.teatime.dto.UserDTO;
import com.teatime.entity.Follow;
import com.teatime.entity.User;
import com.teatime.repository.FollowRepository;
import com.teatime.service.impl.FollowServiceImpl;
import com.teatime.utils.UserHolder;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

  @Mock
  private FollowRepository followRepository;

  @Mock
  private StringRedisTemplate stringRedisTemplate;

  @Mock
  private IUserService userService;

  @Mock
  private SetOperations<String, String> setOperations;

  private FollowServiceImpl followService;

  @org.junit.jupiter.api.BeforeEach
  void setUp() {
    lenient().when(stringRedisTemplate.opsForSet()).thenReturn(setOperations);
    followService = new FollowServiceImpl(followRepository, stringRedisTemplate, userService);
  }

  @Test
  void testFindByFollowUserId_ReturnsFollows() {
    Long followUserId = 2L;
    List<Follow> follows = List.of(createFollow(1L, followUserId));
    when(followRepository.findByFollowUserId(followUserId)).thenReturn(follows);

    List<Follow> result = followService.findByFollowUserId(followUserId);

    assertEquals(1, result.size());
    assertEquals(followUserId, result.get(0).getFollowUserId());
  }

  @Test
  void testIsFollowed_WhenFollowed_ReturnsTrue() {
    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      UserDTO user = new UserDTO();
      user.setId(1L);
      userHolderMock.when(UserHolder::getUser).thenReturn(user);

      when(followRepository.countByUserIdAndFollowUserId(1L, 2L)).thenReturn(1L);

      Result result = followService.isFollowed(2L);

      assertTrue(result.getSuccess());
      assertTrue((Boolean) result.getData());
    }
  }

  @Test
  void testIsFollowed_WhenNotFollowed_ReturnsFalse() {
    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      UserDTO user = new UserDTO();
      user.setId(1L);
      userHolderMock.when(UserHolder::getUser).thenReturn(user);

      when(followRepository.countByUserIdAndFollowUserId(1L, 2L)).thenReturn(0L);

      Result result = followService.isFollowed(2L);

      assertTrue(result.getSuccess());
      assertFalse((Boolean) result.getData());
    }
  }

  @Test
  void testFollowUser_Follow_CreatesFollowAndAddsToRedis() {
    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      UserDTO user = new UserDTO();
      user.setId(1L);
      userHolderMock.when(UserHolder::getUser).thenReturn(user);

      Follow savedFollow = createFollow(1L, 2L);
      when(followRepository.save(any(Follow.class))).thenReturn(savedFollow);

      Result result = followService.followUser(2L, true);

      assertTrue(result.getSuccess());
      verify(followRepository).save(any(Follow.class));
      verify(setOperations).add(eq("follows:1"), eq("2"));
    }
  }

  @Test
  void testFollowUser_Follow_SaveReturnsNull_DoesNotAddToRedis() {
    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      UserDTO user = new UserDTO();
      user.setId(1L);
      userHolderMock.when(UserHolder::getUser).thenReturn(user);

      when(followRepository.save(any(Follow.class))).thenReturn(null);

      Result result = followService.followUser(2L, true);

      assertTrue(result.getSuccess());
      verify(followRepository).save(any(Follow.class));
      verify(setOperations, never()).add(anyString(), anyString());
    }
  }

  @Test
  void testCommonFollow_IntersectReturnsNull_ReturnsEmptyList() {
    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      UserDTO user = new UserDTO();
      user.setId(1L);
      userHolderMock.when(UserHolder::getUser).thenReturn(user);

      when(setOperations.intersect(eq("follows:1"), eq("follows:2"))).thenReturn(null);

      Result result = followService.commonFollow(2L);

      assertTrue(result.getSuccess());
      assertEquals(Collections.emptyList(), result.getData());
    }
  }

  @Test
  void testFollowUser_Unfollow_DeletesAndRemovesFromRedis() {
    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      UserDTO user = new UserDTO();
      user.setId(1L);
      userHolderMock.when(UserHolder::getUser).thenReturn(user);

      Result result = followService.followUser(2L, false);

      assertTrue(result.getSuccess());
      verify(followRepository).deleteByUserIdAndFollowUserId(1L, 2L);
      verify(setOperations).remove(eq("follows:1"), eq("2"));
    }
  }

  @Test
  void testGetFollowerCount_ReturnsCount() {
    when(followRepository.countByFollowUserId(1L)).thenReturn(42L);

    Result result = followService.getFollowerCount(1L);

    assertTrue(result.getSuccess());
    assertEquals(42L, result.getData());
  }

  @Test
  void testGetFollowingCount_ReturnsCount() {
    when(followRepository.countByUserId(1L)).thenReturn(15L);

    Result result = followService.getFollowingCount(1L);

    assertTrue(result.getSuccess());
    assertEquals(15L, result.getData());
  }

  @Test
  void testCommonFollow_NoCommon_ReturnsEmptyList() {
    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      UserDTO user = new UserDTO();
      user.setId(1L);
      userHolderMock.when(UserHolder::getUser).thenReturn(user);

      when(setOperations.intersect(eq("follows:1"), eq("follows:2"))).thenReturn(Collections.emptySet());

      Result result = followService.commonFollow(2L);

      assertTrue(result.getSuccess());
      assertEquals(Collections.emptyList(), result.getData());
    }
  }

  @Test
  void testCommonFollow_WithCommon_ReturnsUsers() {
    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      UserDTO user = new UserDTO();
      user.setId(1L);
      userHolderMock.when(UserHolder::getUser).thenReturn(user);

      Set<String> commonIds = Set.of("3", "4");
      when(setOperations.intersect(eq("follows:1"), eq("follows:2"))).thenReturn(commonIds);

      User user3 = new User();
      user3.setId(3L);
      user3.setNickName("User3");
      User user4 = new User();
      user4.setId(4L);
      user4.setNickName("User4");
      lenient().when(userService.listByIds(anyList())).thenReturn(List.of(user3, user4));

      Result result = followService.commonFollow(2L);

      assertTrue(result.getSuccess());
      @SuppressWarnings("unchecked")
      List<UserDTO> users = (List<UserDTO>) result.getData();
      assertEquals(2, users.size());
    }
  }

  private Follow createFollow(Long userId, Long followUserId) {
    Follow follow = new Follow();
    follow.setUserId(userId);
    follow.setFollowUserId(followUserId);
    return follow;
  }
}
