package com.teatime.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teatime.dto.LoginFormDTO;
import com.teatime.dto.Result;
import com.teatime.entity.User;
import javax.servlet.http.HttpSession;

/**
 * <p>
 * Service interface for user-related operations
 * </p>
 */
public interface IUserService extends IService<User> {

  /**
   * Send verification code to the specified phone number
   *
   * @param phone   Phone number
   * @param session HTTP session
   * @return Result indicating success or failure
   */
  Result sendCode(String phone, HttpSession session);

  /**
   * User login with phone number and verification code
   *
   * @param loginForm Login form data
   * @param session   HTTP session
   * @return Result indicating success or failure
   */
  Result login(LoginFormDTO loginForm, HttpSession session);

  /**
   * User logout
   *
   * @return Result indicating success or failure
   */
  Result checkIn();

  /**
   * Get the count of consecutive check-ins for the current user
   *
   * @return Result with the count of consecutive check-ins
   */
  Result checkInCount();
}
