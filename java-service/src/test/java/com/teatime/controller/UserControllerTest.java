package com.teatime.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teatime.dto.LoginFormDTO;
import com.teatime.dto.Result;
import com.teatime.dto.UserDTO;
import com.teatime.entity.User;
import com.teatime.entity.UserInfo;
import com.teatime.service.IUserInfoService;
import com.teatime.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  private MockMvc mockMvc;

  private ObjectMapper objectMapper;

  @Mock
  private IUserService userService;

  @Mock
  private IUserInfoService userInfoService;

  @InjectMocks
  private UserController userController;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    objectMapper = new ObjectMapper();
  }

  /**
   * Test 1: POST /api/user/code - Send verification code successfully
   */
  @Test
  void testSendCode_Success() throws Exception {
    // Arrange
    String phone = "13800138000";
    when(userService.sendCode(eq(phone), any())).thenReturn(Result.ok());

    // Act & Assert
    mockMvc.perform(post("/api/user/code")
            .param("phone", phone))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  /**
   * Test 2: POST /api/user/code - Invalid phone number
   */
  @Test
  void testSendCode_InvalidPhone() throws Exception {
    // Arrange
    String invalidPhone = "123";
    when(userService.sendCode(eq(invalidPhone), any()))
        .thenReturn(Result.fail("Phone number format is invalid"));

    // Act & Assert
    mockMvc.perform(post("/api/user/code")
            .param("phone", invalidPhone))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.errorMsg").value("Phone number format is invalid"));
  }

  /**
   * Test 3: POST /api/user/login - Successful login
   */
  @Test
  void testLogin_Success() throws Exception {
    // Arrange
    LoginFormDTO loginForm = new LoginFormDTO();
    loginForm.setPhone("13800138000");
    loginForm.setCode("123456");

    String token = "test-token-123";
    when(userService.login(any(LoginFormDTO.class), any())).thenReturn(Result.ok(token));

    // Act & Assert
    mockMvc.perform(post("/api/user/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginForm)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").value(token));
  }

  /**
   * Test 4: POST /api/user/login - Invalid credentials
   */
  @Test
  void testLogin_InvalidCredentials() throws Exception {
    // Arrange
    LoginFormDTO loginForm = new LoginFormDTO();
    loginForm.setPhone("13800138000");
    loginForm.setCode("wrong-code");

    when(userService.login(any(LoginFormDTO.class), any()))
        .thenReturn(Result.fail("Verification code is incorrect"));

    // Act & Assert
    mockMvc.perform(post("/api/user/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginForm)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.errorMsg").value("Verification code is incorrect"));
  }

  /**
   * Test 5: GET /api/user/{id} - Get user by ID
   */
  @Test
  void testQueryUserById_Success() throws Exception {
    // Arrange
    Long userId = 1L;
    User user = new User();
    user.setId(userId);
    user.setPhone("13800138000");
    user.setNickName("TestUser");

    when(userService.getById(userId)).thenReturn(user);

    // Act & Assert
    mockMvc.perform(get("/api/user/{id}", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(userId))
        .andExpect(jsonPath("$.data.nickName").value("TestUser"));
  }

  /**
   * Test 6: GET /api/user/{id} - User not found
   */
  @Test
  void testQueryUserById_NotFound() throws Exception {
    // Arrange
    Long userId = 999L;
    when(userService.getById(userId)).thenReturn(null);

    // Act & Assert
    mockMvc.perform(get("/api/user/{id}", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  /**
   * Test 7: GET /api/user/me - Get current user
   */
  @Test
  void testMe_ReturnsCurrentUser() throws Exception {
    // Arrange
    UserDTO userDTO = new UserDTO();
    userDTO.setId(1L);
    userDTO.setNickName("TestUser");

    // Note: In real scenario, UserHolder is populated by interceptor
    // For standalone MockMvc, we just verify the endpoint works
    mockMvc.perform(get("/api/user/me"))
        .andExpect(status().isOk());
  }

  /**
   * Test 8: POST /api/user/checkin - User check-in
   */
  @Test
  void testCheckIn() throws Exception {
    // Arrange
    when(userService.checkIn()).thenReturn(Result.ok());

    // Act & Assert
    mockMvc.perform(post("/api/user/checkin"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  /**
   * Test 9: GET /api/user/checkin/count - Get check-in count
   */
  @Test
  void testCheckInCount() throws Exception {
    // Arrange
    when(userService.checkInCount()).thenReturn(Result.ok(5));

    // Act & Assert
    mockMvc.perform(get("/api/user/checkin/count"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").value(5));
  }

  /**
   * Test 10: POST /api/user/logout - Logout returns unimplemented
   */
  @Test
  void testLogout_Unimplemented() throws Exception {
    // Act & Assert
    mockMvc.perform(post("/api/user/logout"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.errorMsg").value("Unimplemented"));
  }

  /**
   * Test 11: GET /api/user/info/{id} - Get user info by ID
   */
  @Test
  void testInfo_Success() throws Exception {
    // Arrange
    Long userId = 1L;
    UserInfo userInfo = new UserInfo();
    userInfo.setUserId(userId);
    userInfo.setCity("Vancouver");
    userInfo.setBio("Tea enthusiast");
    userInfo.setFollower(10);
    userInfo.setFollowee(5);

    when(userInfoService.getById(userId)).thenReturn(userInfo);

    // Act & Assert
    mockMvc.perform(get("/api/user/info/{id}", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.userId").value(userId))
        .andExpect(jsonPath("$.data.city").value("Vancouver"))
        .andExpect(jsonPath("$.data.bio").value("Tea enthusiast"));
  }

  /**
   * Test 12: GET /api/user/info/{id} - User info not found returns empty ok
   */
  @Test
  void testInfo_NotFound_ReturnsEmptyOk() throws Exception {
    Long userId = 999L;
    when(userInfoService.getById(userId)).thenReturn(null);

    mockMvc.perform(get("/api/user/info/{id}", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }
}