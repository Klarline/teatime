package com.teatime.controller;


import com.teatime.dto.Result;
import com.teatime.service.IFollowService;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Follow controller for managing user follow relationships.
 * </p>
 */
@RestController
@RequestMapping("/api/follow")
public class FollowController {

  @Resource
  private IFollowService followService;

  /**
   * Follow or unfollow a user.
   * PUT /api/follow/{id}/{isFollow}
   * <p>
   * Body: none
   */
  @PutMapping("/{id}/{isFollow}")
  public Result followUser(@PathVariable("id") Long followUserId,
                           @PathVariable("isFollow") Boolean isFollow) {
    return followService.followUser(followUserId, isFollow);
  }

  /**
   * Check if the current user follows another user.
   * GET /api/follow/or/not/{id}
   * <p>
   * Returns: { isFollowed: true/false }
   */
  @GetMapping("/or/not/{id}")
  public Result isFollowed(@PathVariable("id") Long followUserId) {
    return followService.isFollowed(followUserId);
  }

  /**
   * Get common followers between the current user and another user.
   * GET /api/follow/common/{id}
   * <p>
   * Returns: { list: [UserDTO, UserDTO, ...] }
   */
  @GetMapping("/common/{id}")
  public Result commonFollow(@PathVariable("id") Long id) {
    return followService.commonFollow(id);
  }

  /**
   * Get the count of followers for a user.
   * GET /api/follow/followers/count/{id}
   * <p>
   * Returns: { count: number }
   */
  @GetMapping("/followers/count/{id}")
  public Result getFollowerCount(@PathVariable("id") Long userId) {
    return followService.getFollowerCount(userId);
  }

  /**
   * Get the count of users a user is following.
   * GET /api/follow/following/count/{id}
   * <p>
   * Returns: { count: number }
   */
  @GetMapping("/following/count/{id}")
  public Result getFollowingCount(@PathVariable("id") Long userId) {
    return followService.getFollowingCount(userId);
  }
}
