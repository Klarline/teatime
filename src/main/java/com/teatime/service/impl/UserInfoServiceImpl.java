package com.teatime.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teatime.entity.UserInfo;
import com.teatime.mapper.UserInfoMapper;
import com.teatime.service.IUserInfoService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * User information service implementation
 * </p>
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo>
    implements IUserInfoService {

}
