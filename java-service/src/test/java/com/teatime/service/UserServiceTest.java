package com.teatime.service;

import static com.teatime.utils.RedisConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teatime.dto.LoginFormDTO;
import com.teatime.dto.Result;
import com.teatime.dto.UserDTO;
import com.teatime.entity.User;
import com.teatime.mapper.UserMapper;
import com.teatime.service.impl.UserServiceImpl;
import com.teatime.utils.RegexUtils;
import com.teatime.utils.UserHolder;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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
  private UserMapper userMapper;

  @Mock
  private HttpSession session;

  @InjectMocks
  private UserServiceImpl userService;

  /**
   * Test 1: Send code with valid phone number - should succeed
   */
  @Test
  void testSendCode_ValidPhone_Success() {
    // Arrange
    String phone = "6045551234";
    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

    try (MockedStatic<RegexUtils> regexUtils = mockStatic(RegexUtils.class)) {
      regexUtils.when(() -> RegexUtils.isPhoneInvalid(phone)).thenReturn(false);

      // Act
      Result result = userService.sendCode(phone, session);

      // Assert
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
    // Arrange
    String invalidPhone = "123";

    try (MockedStatic<RegexUtils> regexUtils = mockStatic(RegexUtils.class)) {
      regexUtils.when(() -> RegexUtils.isPhoneInvalid(invalidPhone)).thenReturn(true);

      // Act
      Result result = userService.sendCode(invalidPhone, session);

      // Assert
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
    // Arrange
    LoginFormDTO loginForm = new LoginFormDTO();
    loginForm.setPhone("6045551234");
    loginForm.setCode("123456");

    String cacheKey = LOGIN_CODE_KEY + loginForm.getPhone();

    // Set up mocks
    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);
    when(valueOperations.get(cacheKey)).thenReturn("123456");

    // Mock existing user
    User existingUser = new User();
    existingUser.setId(1L);
    existingUser.setPhone(loginForm.getPhone());
    existingUser.setNickName("TestUser");

    when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(existingUser);

    try (MockedStatic<RegexUtils> regexUtils = mockStatic(RegexUtils.class);
         MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {

      regexUtils.when(() -> RegexUtils.isPhoneInvalid(anyString())).thenReturn(false);

      UUID mockUuid = mock(UUID.class);
      when(mockUuid.toString(true)).thenReturn("test-token-123");
      uuidMock.when(() -> UUID.randomUUID()).thenReturn(mockUuid);

      // Act
      Result result = userService.login(loginForm, session);

      // Assert
      assertTrue(result.getSuccess());
      assertEquals("test-token-123", result.getData());

      // Verify token was saved to Redis
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
    // Arrange
    LoginFormDTO loginForm = new LoginFormDTO();
    loginForm.setPhone("6045551234");
    loginForm.setCode("123456");

    String cacheKey = LOGIN_CODE_KEY + loginForm.getPhone();

    // Set up mocks
    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(cacheKey)).thenReturn("654321");

    try (MockedStatic<RegexUtils> regexUtils = mockStatic(RegexUtils.class)) {
      regexUtils.when(() -> RegexUtils.isPhoneInvalid(anyString())).thenReturn(false);

      // Act
      Result result = userService.login(loginForm, session);

      // Assert
      assertFalse(result.getSuccess());
      assertEquals("Verification code is incorrect", result.getErrorMsg());
      verify(userMapper, never()).selectOne(any());
    }
  }

  /**
   * Test 5: Login with expired/missing code - should fail
   */
  @Test
  void testLogin_MissingCode_ReturnsFail() {
    // Arrange
    LoginFormDTO loginForm = new LoginFormDTO();
    loginForm.setPhone("6045551234");
    loginForm.setCode("123456");

    String cacheKey = LOGIN_CODE_KEY + loginForm.getPhone();

    // Set up mocks
    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(cacheKey)).thenReturn(null);

    try (MockedStatic<RegexUtils> regexUtils = mockStatic(RegexUtils.class)) {
      regexUtils.when(() -> RegexUtils.isPhoneInvalid(anyString())).thenReturn(false);

      // Act
      Result result = userService.login(loginForm, session);

      // Assert
      assertFalse(result.getSuccess());
      assertEquals("Verification code is incorrect", result.getErrorMsg());
      verify(userMapper, never()).selectOne(any());
    }
  }

  /**
   * Test 6: Login with new user (user doesn't exist) - should create user and return token
   */
  @Test
  void testLogin_NewUser_CreatesUserAndReturnsToken() {
    // Arrange
    LoginFormDTO loginForm = new LoginFormDTO();
    loginForm.setPhone("6045551234");
    loginForm.setCode("123456");

    String cacheKey = LOGIN_CODE_KEY + loginForm.getPhone();

    // Set up mocks
    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);
    when(valueOperations.get(cacheKey)).thenReturn("123456");
    when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

    // Mock successful user creation
    when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
      User user = invocation.getArgument(0);
      user.setId(1L);
      return 1;
    });

    try (MockedStatic<RegexUtils> regexUtils = mockStatic(RegexUtils.class);
         MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {

      regexUtils.when(() -> RegexUtils.isPhoneInvalid(anyString())).thenReturn(false);

      UUID mockUuid = mock(UUID.class);
      when(mockUuid.toString(true)).thenReturn("test-token-456");
      uuidMock.when(() -> UUID.randomUUID()).thenReturn(mockUuid);

      // Act
      Result result = userService.login(loginForm, session);

      // Assert
      assertTrue(result.getSuccess());
      assertEquals("test-token-456", result.getData());

      // Verify new user was created
      verify(userMapper).insert(any(User.class));

      // Verify token was saved to Redis
      verify(hashOperations).putAll(eq(LOGIN_USER_KEY + "test-token-456"), anyMap());
    }
  }

  /**
   * Test 7: Login with invalid phone format - should fail immediately
   */
  @Test
  void testLogin_InvalidPhone_ReturnsFail() {
    // Arrange
    LoginFormDTO loginForm = new LoginFormDTO();
    loginForm.setPhone("invalid");
    loginForm.setCode("123456");

    try (MockedStatic<RegexUtils> regexUtils = mockStatic(RegexUtils.class)) {
      regexUtils.when(() -> RegexUtils.isPhoneInvalid("invalid")).thenReturn(true);

      // Act
      Result result = userService.login(loginForm, session);

      // Assert
      assertFalse(result.getSuccess());
      assertEquals("Phone number format is invalid", result.getErrorMsg());

      // Verify no Redis or DB operations were performed
      verify(stringRedisTemplate, never()).opsForValue();
      verify(userMapper, never()).selectOne(any());
    }
  }

  /**
   * Test 8: CheckIn - successful check-in for current day
   */
  @Test
  void testCheckIn_Success() {
    // Arrange
    UserDTO userDTO = new UserDTO();
    userDTO.setId(1L);

    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(userDTO);

      // Act
      Result result = userService.checkIn();

      // Assert
      assertTrue(result.getSuccess());

      // Verify Redis setBit was called
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
    // Arrange
    UserDTO userDTO = new UserDTO();
    userDTO.setId(1L);

    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.bitField(anyString(), any())).thenReturn(null);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(userDTO);

      // Act
      Result result = userService.checkInCount();

      // Assert
      assertTrue(result.getSuccess());
      assertEquals(0, result.getData());
    }
  }

  /**
   * Test 10: CheckInCount - counts consecutive check-in days correctly
   */
  @Test
  void testCheckInCount_ConsecutiveDays_ReturnsCount() {
    // Arrange
    UserDTO userDTO = new UserDTO();
    userDTO.setId(1L);

    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

    // Mock bitmap: last 3 days checked in (binary: ...00111 = 7 in decimal)
    // This means 3 consecutive days
    List<Long> bitFieldResult = List.of(7L);
    when(valueOperations.bitField(anyString(), any())).thenReturn(bitFieldResult);

    try (MockedStatic<UserHolder> userHolderMock = mockStatic(UserHolder.class)) {
      userHolderMock.when(UserHolder::getUser).thenReturn(userDTO);

      // Act
      Result result = userService.checkInCount();

      // Assert
      assertTrue(result.getSuccess());
      assertEquals(3, result.getData());
    }
  }
}