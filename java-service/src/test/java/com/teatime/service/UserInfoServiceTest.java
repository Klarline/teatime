package com.teatime.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.teatime.entity.UserInfo;
import com.teatime.repository.UserInfoRepository;
import com.teatime.service.impl.UserInfoServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserInfoServiceTest {

  @Mock
  private UserInfoRepository userInfoRepository;

  @InjectMocks
  private UserInfoServiceImpl userInfoService;

  @Test
  void testGetById_Exists_ReturnsUserInfo() {
    UserInfo userInfo = new UserInfo();
    userInfo.setUserId(1L);
    userInfo.setCity("Vancouver");
    when(userInfoRepository.findById(1L)).thenReturn(Optional.of(userInfo));

    UserInfo result = userInfoService.getById(1L);

    assertNotNull(result);
    assertEquals(1L, result.getUserId());
    assertEquals("Vancouver", result.getCity());
  }

  @Test
  void testGetById_NotExists_ReturnsNull() {
    when(userInfoRepository.findById(999L)).thenReturn(Optional.empty());

    UserInfo result = userInfoService.getById(999L);

    assertNull(result);
  }
}
