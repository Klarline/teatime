package com.teatime.service;

import com.teatime.dto.LoginFormDTO;
import com.teatime.dto.Result;
import com.teatime.entity.User;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * <p>
 * Service interface for user-related operations
 * </p>
 */
public interface IUserService {

  Result sendCode(String phone, HttpSession session);

  Result login(LoginFormDTO loginForm, HttpSession session);

  Result checkIn();

  Result checkInCount();

  User getById(Long id);

  List<User> listByIds(List<Long> ids);
}
