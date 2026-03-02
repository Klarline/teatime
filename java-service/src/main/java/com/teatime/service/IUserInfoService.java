package com.teatime.service;

import com.teatime.entity.UserInfo;

/**
 * <p>
 * User Info service interface
 * </p>
 */
public interface IUserInfoService {

  UserInfo getById(Long id);
}
