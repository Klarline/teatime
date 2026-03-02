package com.teatime.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.teatime.dto.Result;
import com.teatime.dto.UserDTO;
import com.teatime.entity.Follow;
import com.teatime.repository.FollowRepository;
import com.teatime.service.IFollowService;
import com.teatime.service.IUserService;
import com.teatime.utils.UserHolder;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Follow service implementation
 * </p>
 */
@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements IFollowService {

  private final FollowRepository followRepository;
  private final StringRedisTemplate stringRedisTemplate;
  private final IUserService userService;

  @Override
  public List<Follow> findByFollowUserId(Long followUserId) {
    return followRepository.findByFollowUserId(followUserId);
  }

  @Override
  public Result isFollowed(Long followUserId) {
    Long userId = UserHolder.getUser().getId();
    long count = followRepository.countByUserIdAndFollowUserId(userId, followUserId);
    return Result.ok(count > 0);
  }

  @Override
  public Result followUser(Long followUserId, Boolean isFollow) {
    Long userId = UserHolder.getUser().getId();
    String key = "follows:" + userId;
    if (isFollow) {
      Follow follow = new Follow();
      follow.setUserId(userId);
      follow.setFollowUserId(followUserId);
      Follow saved = followRepository.save(follow);
      if (saved != null) {
        stringRedisTemplate.opsForSet().add(key, followUserId.toString());
      }
    } else {
      followRepository.deleteByUserIdAndFollowUserId(userId, followUserId);
      stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
    }
    return Result.ok();
  }

  @Override
  public Result commonFollow(Long id) {
    Long userId = UserHolder.getUser().getId();
    String key1 = "follows:" + userId;
    String key2 = "follows:" + id;

    Set<String> intersect = stringRedisTemplate.opsForSet().intersect(key1, key2);

    if (intersect == null || intersect.isEmpty()) {
      return Result.ok(Collections.emptyList());
    }

    List<Long> ids = intersect.stream().map(Long::valueOf).collect(Collectors.toList());
    List<UserDTO> users = userService.listByIds(ids).stream()
        .map(user -> BeanUtil.copyProperties(user, UserDTO.class)).collect(Collectors.toList());

    return Result.ok(users);
  }

  @Override
  public Result getFollowerCount(Long userId) {
    long count = followRepository.countByFollowUserId(userId);
    return Result.ok(count);
  }

  @Override
  public Result getFollowingCount(Long userId) {
    long count = followRepository.countByUserId(userId);
    return Result.ok(count);
  }
}
