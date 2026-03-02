package com.teatime.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;

class FallbackSessionStoreTest {

  @Test
  void setLoginCodeAndGetLoginCode_StoresAndRetrieves() {
    String phone = "6045559999";
    String code = "123456";

    FallbackSessionStore.setLoginCode(phone, code);
    String retrieved = FallbackSessionStore.getLoginCode(phone);

    assertEquals(code, retrieved);
  }

  @Test
  void getLoginCode_NotSet_ReturnsNull() {
    String retrieved = FallbackSessionStore.getLoginCode("6045550000");
    assertNull(retrieved);
  }

  @Test
  void setSessionAndGetSession_StoresAndRetrieves() {
    String token = "test-token-123";
    Map<Object, Object> userMap = Map.of(
        "id", 1L,
        "nickName", "TestUser"
    );

    FallbackSessionStore.setSession(token, userMap);
    Map<Object, Object> retrieved = FallbackSessionStore.getSession(token);

    assertEquals(userMap, retrieved);
  }

  @Test
  void getSession_NotSet_ReturnsEmptyMap() {
    Map<Object, Object> retrieved = FallbackSessionStore.getSession("nonexistent-token");
    assertNotNull(retrieved);
    assertTrue(retrieved.isEmpty());
  }

  @Test
  void refreshSession_ExtendsExpiry() {
    String token = "refresh-token-456";
    Map<Object, Object> userMap = Map.of("id", 2L);

    FallbackSessionStore.setSession(token, userMap);
    FallbackSessionStore.refreshSession(token);

    Map<Object, Object> retrieved = FallbackSessionStore.getSession(token);
    assertEquals(userMap, retrieved);
  }

  @Test
  void refreshSession_NonExistentToken_DoesNotThrow() {
    assertDoesNotThrow(() -> FallbackSessionStore.refreshSession("never-set-token"));
  }
}
