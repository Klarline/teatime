package com.teatime.service.impl;

import com.teatime.entity.UserInfo;
import com.teatime.repository.UserInfoRepository;
import com.teatime.service.IUserInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * User Info service implementation
 * </p>
 */
@Service
public class UserInfoServiceImpl implements IUserInfoService {

  @Resource
  private UserInfoRepository userInfoRepository;

  @Override
  public UserInfo getById(Long id) {
    return userInfoRepository.findById(id).orElse(null);
  }
}
