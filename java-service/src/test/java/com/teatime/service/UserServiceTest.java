package com.teatime.service;

import static com.teatime.utils.RedisConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import cn.hutool.core.lang.UUID;
import com.teatime.dto.LoginFormDTO;
import com.teatime.dto.Result;
import com.teatime.dto.UserDTO;
import com.teatime.entity.User;
import com.teatime.repository.UserRepository;
import com.teatime.service.impl.UserServiceImpl;
import com.teatime.utils.RegexUtils;
import com.teatime.utils.UserHolder;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.servlet.http.HttpSession;
import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private StringRedisTemplate stringRedisTemplate;

  @Mock
  private ValueOperations<String, String> valueOperations;

  @Mock
  private HashOperations<String, Object, Object> hashOperations;

  @Mock
  private UserRepository userRepository;

  @Mock
  private HttpSession session;

  @InjectMocks
  private UserServiceImpl userService;

  /**
   * Test 1: Send code with valid phone number - should succeed
   */
  @Test
  void testSendCode_ValidPhone_Success() {
    String phone = "6045551234";
    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

    try (MockedStatic<RegexUtils> regexUtils = mockStatic(RegexUtils.class)) {
      regexUtils.when(() -> RegexUtils.isPhoneInvalid(phone)).thenReturn(false);

      Result result = userService.sendCode(phone, session);

      assertTrue(result.getSuccess());
      verify(valueOperations).set(
          eq(LOGIN_CODE_KEY + phone),
          anyString(),
          eq(LOGIN_CODE_TTL),
          eq(TimeUnit.MINUTES)
      );
    }
  }

  /**
   * Test 2: Send code with invalid phone number - should fail
   */
  @Test
  void testSendCode_InvalidPhone_ReturnsFail() {
    String invalidPhone = "123";

    try (MockedStatic<RegexUtils> regexUtils = mockStatic(RegexUtils.class)) {
      regexUtils.when(() -> RegexUtils.isPhoneInvalid(invalidPhone)).thenReturn(true);

      Result result = userService.sendCode(invalidPhone, session);

      assertFalse(result.getSuccess());
      assertEquals("Phone number format is invalid", result.getErrorMsg());
      verify(stringRedisTemplate, never()).opsForValue();
    }
  }

  /**
   * Test 3: Login with valid credentials and existing user - should return token
   */
  @Test
  void testLogin_ValidCredentials_ExistingUser_ReturnsToken() {
    LoginFormDTO loginForm = new LoginFormDTO();
    loginForm.setPhone("6045551234");
    loginForm.setCode("123456");

    String cacheKey = LOGIN_CODE_KEY + loginForm.getPhone();

    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);
    when(valueOperations.get(cacheKey)).thenReturn("123456");

    User existingUser = new User();
    existingUser.setId(1L);
    existingUser.setPhone(loginForm.getPhone());
    existingUser.setNickName("TestUser");

    when(userRepository.findByPhone(anyString())).thenReturn(Optional.of(existingUser));

    try (MockedStatic<RegexUtils> regexUtils = mockStatic(RegexUtils.class);
         MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {

      regexUtils.when(() -> RegexUtils.isPhoneInvalid(anyString())).thenReturn(false);

      UUID mockUuid = mock(UUID.class);
      when(mockUuid.toString(true)).thenReturn("test-token-123");
      uuidMock.when(() -> UUID.randomUUID()).thenReturn(mockUuid);

      Result result = userService.login(loginForm, session);

      assertTrue(result.getSuccess());
      assertEquals("test-token-123", result.getData());

      verify(hashOperations).putAll(eq(LOGIN_USER_KEY + "test-token-123"), anyMap());
      verify(stringRedisTemplate).expire(
          eq(LOGIN_USER_KEY + "test-token-123"),
          eq(LOGIN_USER_TTL),
          eq(TimeUnit.SECONDS)
      );
    }
  }

  /**
   * Test 4: Login with incorrect verification code - should fail
   */
  @Test
  void testLogin_IncorrectCode_ReturnsFail() {
    LoginFormDTO loginForm = new LoginFormDTO();
    loginForm.setPhone("6045551234");
    loginForm.setCode("123456");

    String cacheKey = LOGIN_CODE_KEY + loginForm.getPhone();

    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(cacheKey)).thenReturn("654321");

    try (MockedStatic<RegexUtils> regexUtils = mockStatic(RegexUtils.class)) {
      regexUtils.when(() -> RegexUtils.isPhoneInvalid(anyString())).thenReturn(false);

      Result result = userService.login(loginForm, session);

      assertFalse(result.getSuccess());
      assertEquals("Verification code is incorrect", result.getErrorMsg());
      verify(userRepository, never()).findByPhone(any());
    }
  }

  /**
   * Test 5: Login with expired/missing code - should fail
   */
  @Test
  void testLogin_MissingCode_ReturnsFail() {
    LoginFormDTO loginForm = new LoginFormDTO();
    loginForm.setPhone("6045551234");
    loginForm.setCode("123456");

    String cacheKey = LOGIN_CODE_KEY + loginForm.getPhone();

    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(cacheKey)).thenReturn(null);

    try (MockedStatic<RegexUtils> regexUtils = mockStatic(RegexUtils.class)) {
      regexUtils.when(() -> RegexUtils.isPhoneInvalid(anyString())).thenReturn(false);

      Result result = userService.login(loginForm, session);

      assertFalse(result.getSuccess());
      assertEquals("Verification code is incorrect", result.getErrorMsg());
      verify(userRepository, never()).findByPhone(any());
    }
  }

  /**
   * Test 6: Login with new user (user doesn't exist) - should create user and return token
   */
  @Test
  void testLogin_NewUser_CreatesUserAndReturnsToken() {
    LoginFormDTO loginForm = new LoginFormDTO();
    loginForm.setPhone("6045551234");
    loginForm.setCode("123456");

    String cacheKey = LOGIN_CODE_KEY + loginForm.getPhone();

    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);
    when(valueOperations.get(cacheKey)).thenReturn("123456");
    when(userRepository.findByPhone(anyString())).thenReturn(Optional.empty());

    when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
      User user = invocation.getArgument(0);
      user.setId(1L);
      return user;
    });

    try (MockedStatic<RegexUtils> regexUtils = mockStatic(RegexUtils.class);
         MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {

      regexUtils.when(() -> RegexUtils.isPhoneInvalid(anyString())).thenReturn(false);

      UUID mockUuid = mock(UUID.class);
      when(mockUuid.toString(true)).thenReturn("test-token-456");
      uuidMock.when(() -> UUID.randomUUID()).thenReturn(mockUuid);

      Result result = userService.login(loginForm, session);

      assertTrue(result.getSuccess());
      assertEquals("test-token-456", result.getData());

      verify(userRepository).save(any(User.class));

      verify(hashOperations).putAll(eq(LOGIN_USER_KEY + "test-token-456"), anyMap());
    }
  }

  /**
   * Test 7: Login with invalid phone format - should fail immediately
   */
  @Test
  void testLogin_InvalidPhone_ReturnsFail() {
    LoginFormDTO loginForm = new LoginFormDTO();
    loginForm.setPhone("invalid");
    loginForm.setCode("123456");

    try (MockedStatic<RegexUtils> regexUtils = mockStatic(RegexUtils.class)) {
      regexUtils.when(() -> RegexUtils.isPhoneInvalid("invalid")).thenReturn(true);

      Result result = userService.login(loginForm, session);

      assertFalse(result.getSuccess());
      assertEquals("Phone number format is invalid", result.getErrorMsg());

      verify(stringRedisTemplate, never()).opsForValue();
      verify(userRepository, never()).findByPhone(any());
    }
  }

  /**
   * Test 8: CheckIn - successful check-in for current day
   */
  @Test
  void testCheckIn_Success() {
    UserDTO userDTO = new UserDTO();
    userDTO.setId(1L);

    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(userDTO);

      Result result = userService.checkIn();

      assertTrue(result.getSuccess());

      verify(valueOperations).setBit(
          contains("user:checkin:1"),
          anyLong(),
          eq(true)
      );
    }
  }

  /**
   * Test 9: CheckInCount - no check-ins returns 0
   */
  @Test
  void testCheckInCount_NoCheckIns_ReturnsZero() {
    UserDTO userDTO = new UserDTO();
    userDTO.setId(1L);

    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.bitField(anyString(), any())).thenReturn(null);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(userDTO);

      Result result = userService.checkInCount();

      assertTrue(result.getSuccess());
      assertEquals(0, result.getData());
    }
  }

  /**
   * Test 10: CheckInCount - counts consecutive check-in days correctly
   */
  @Test
  void testCheckInCount_ConsecutiveDays_ReturnsCount() {
    UserDTO userDTO = new UserDTO();
    userDTO.setId(1L);

    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

    List<Long> bitFieldResult = List.of(7L);
    when(valueOperations.bitField(anyString(), any())).thenReturn(bitFieldResult);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(userDTO);

      Result result = userService.checkInCount();

      assertTrue(result.getSuccess());
      assertEquals(3, result.getData());
    }
  }

  @Test
  void testCheckInCount_EmptyBitFieldList_ReturnsZero() {
    UserDTO userDTO = new UserDTO();
    userDTO.setId(1L);

    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.bitField(anyString(), any())).thenReturn(List.of());

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(userDTO);

      Result result = userService.checkInCount();

      assertTrue(result.getSuccess());
      assertEquals(0, result.getData());
    }
  }

  @Test
  void testCheckInCount_NumZero_ReturnsZero() {
    UserDTO userDTO = new UserDTO();
    userDTO.setId(1L);

    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.bitField(anyString(), any())).thenReturn(List.of(0L));

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(userDTO);

      Result result = userService.checkInCount();

      assertTrue(result.getSuccess());
      assertEquals(0, result.getData());
    }
  }

  @Test
  void testGetById_Exists_ReturnsUser() {
    User user = new User();
    user.setId(1L);
    user.setNickName("TestUser");
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    User result = userService.getById(1L);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("TestUser", result.getNickName());
  }

  @Test
  void testGetById_NotExists_ReturnsNull() {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    User result = userService.getById(999L);

    assertNull(result);
  }

  @Test
  void testListByIds_ReturnsUsers() {
    User user1 = new User();
    user1.setId(1L);
    User user2 = new User();
    user2.setId(2L);
    when(userRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(user1, user2));

    List<User> result = userService.listByIds(List.of(1L, 2L));

    assertEquals(2, result.size());
    assertEquals(1L, result.get(0).getId());
    assertEquals(2L, result.get(1).getId());
  }
}
