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

  Result sendCode(String phone, HttpSession session);

  Result login(LoginFormDTO loginForm, HttpSession session);

  Result checkIn();

  Result checkInCount();
}
