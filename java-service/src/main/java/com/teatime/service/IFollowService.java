package com.teatime.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teatime.entity.Follow;
import com.teatime.dto.Result;

/**
 * <p>
 * Follow service interface
 * </p>
 */
public interface IFollowService extends IService<Follow> {

  /**
   * Check if the current user follows another user
   *
   * @param followUserId ID of the user to check
   * @return Result with boolean
   */
  Result isFollowed(Long followUserId);

  /**
   * Follow or unfollow a user
   *
   * @param followUserId ID of the user to follow/unfollow
   * @param isFollow     true to follow, false to unfollow
   * @return Result
   */
  Result followUser(Long followUserId, Boolean isFollow);

  /**
   * Get common followers between the current user and another user
   *
   * @param id ID of the other user
   * @return Result with list of common followers
   */
  Result commonFollow(Long id);

  /**
   * Get the count of followers for a user
   *
   * @param userId ID of the user
   * @return Result with follower count
   */
  Result getFollowerCount(Long userId);

  /**
   * Get the count of users a user is following
   *
   * @param userId ID of the user
   * @return Result with following count
   */
  Result getFollowingCount(Long userId);
}
