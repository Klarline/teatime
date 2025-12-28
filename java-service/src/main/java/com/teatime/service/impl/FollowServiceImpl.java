package com.teatime.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teatime.dto.Result;
import com.teatime.dto.UserDTO;
import com.teatime.entity.Follow;
import com.teatime.mapper.FollowMapper;
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
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

  private final StringRedisTemplate stringRedisTemplate;
  private final IUserService userService;

  @Override
  public Result isFollowed(Long followUserId) {
    Long userId = UserHolder.getUser().getId();
    Long count = query().eq("user_id", userId).eq("followed_user_id", followUserId).count();
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
      boolean isSuccess = save(follow);

      if (isSuccess) {
        // follow success, add to redis set
        stringRedisTemplate.opsForSet().add(key, followUserId.toString());
      }
    } else {
      // unfollow
      boolean isSuccess = remove(
          new QueryWrapper<Follow>().eq("user_id", userId).eq("followed_user_id", followUserId));
      if (isSuccess) {
        // unfollow success, remove from redis set
        stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
      }
    }
    return Result.ok();
  }

  @Override
  public Result commonFollow(Long id) {
    // get current user id
    Long userId = UserHolder.getUser().getId();
    String key1 = "follows:" + userId;

    String key2 = "follows:" + id;

    // get intersection of two sets
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
    Long count = query().eq("followed_user_id", userId).count();
    return Result.ok(count);
  }

  @Override
  public Result getFollowingCount(Long userId) {
    Long count = query().eq("user_id", userId).count();
    return Result.ok(count);
  }
}

