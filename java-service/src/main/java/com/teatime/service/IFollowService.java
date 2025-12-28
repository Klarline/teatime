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

  Result isFollowed(Long followUserId);

  Result followUser(Long followUserId, Boolean isFollow);

  Result commonFollow(Long id);

  Result getFollowerCount(Long userId);

  Result getFollowingCount(Long userId);
}
