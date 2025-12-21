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

  @PutMapping("/{id}/{isFollow}")
  public Result followUser(@PathVariable("id") Long followUserId,
                           @PathVariable("isFollow") Boolean isFollow) {
    return followService.followUser(followUserId, isFollow);
  }

  @GetMapping("/or/not/{id}")
  public Result isFollowed(@PathVariable("id") Long followUserId) {
    return followService.isFollowed(followUserId);
  }

  @GetMapping("/common/{id}")
  public Result commonFollow(@PathVariable("id") Long id) {
    return followService.commonFollow(id);
  }

}
