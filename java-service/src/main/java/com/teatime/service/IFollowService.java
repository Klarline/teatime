package com.teatime.service;

import com.teatime.dto.Result;
import com.teatime.entity.Follow;

import java.util.List;

/**
 * <p>
 * Follow service interface
 * </p>
 */
public interface IFollowService {

  List<Follow> findByFollowUserId(Long followUserId);

  Result isFollowed(Long followUserId);

  Result followUser(Long followUserId, Boolean isFollow);

  Result commonFollow(Long id);

  Result getFollowerCount(Long userId);

  Result getFollowingCount(Long userId);
}
