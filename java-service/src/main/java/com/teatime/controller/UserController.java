package com.teatime.controller;


import cn.hutool.core.bean.BeanUtil;
import com.teatime.dto.LoginFormDTO;
import com.teatime.dto.Result;
import com.teatime.dto.UserDTO;
import com.teatime.entity.User;
import com.teatime.entity.UserInfo;
import com.teatime.service.IUserInfoService;
import com.teatime.service.IUserService;
import com.teatime.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * <p>
 * Front-end controller for user-related operations
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

  @Resource
  private IUserService userService;

  @Resource
  private IUserInfoService userInfoService;

  /**
   * Send verification code to specified phone number
   */
  @PostMapping("/code")
  public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
    // delegate to user service to send the code
    return userService.sendCode(phone, session);
  }

  /**
   * Login functionality
   *
   * @param loginForm Login form data including phone number and verification code
   */
  @PostMapping("/login")
  public Result login(@RequestBody LoginFormDTO loginForm, HttpSession session) {

    return userService.login(loginForm, session);
  }

  @PostMapping("/logout")
  public Result logout() {
    return Result.fail("Unimplemented");
  }

  @GetMapping("/me")
  public Result me() {
    // get current logged-in user
    UserDTO user = UserHolder.getUser();
    return Result.ok(user);
  }

  @GetMapping("/info/{id}")
  public Result info(@PathVariable("id") Long userId) {
    UserInfo info = userInfoService.getById(userId);
    if (info == null) {
      return Result.ok();
    }
    info.setCreateTime(null);
    info.setUpdateTime(null);

    return Result.ok(info);
  }

  @GetMapping("/{id}")
  public Result queryUserById(@PathVariable("id") Long userId) {
    User user = userService.getById(userId);
    if (user == null) {
      return Result.ok();
    }
    UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
    return Result.ok(userDTO);
  }

  @PostMapping("/checkin")
  public Result checkIn() {
    return userService.checkIn();
  }

  @GetMapping("/checkin/count")
  public Result checkInCount() {
    return userService.checkInCount();
  }
}
