package com.teatime.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PasswordEncoderTest {

  @Test
  void testEncode_ProducesValidFormat() {
    String encoded = PasswordEncoder.encode("mypassword");
    assertNotNull(encoded);
    assertTrue(encoded.contains("@"));
    String[] parts = encoded.split("@");
    assertEquals(2, parts.length);
    assertEquals(20, parts[0].length());
  }

  @Test
  void testMatches_ValidPassword_ReturnsTrue() {
    String encoded = PasswordEncoder.encode("mypassword");
    assertTrue(PasswordEncoder.matches(encoded, "mypassword"));
  }

  @Test
  void testMatches_InvalidPassword_ReturnsFalse() {
    String encoded = PasswordEncoder.encode("mypassword");
    assertFalse(PasswordEncoder.matches(encoded, "wrongpassword"));
  }

  @Test
  void testMatches_NullEncoded_ReturnsFalse() {
    assertFalse(PasswordEncoder.matches(null, "password"));
  }

  @Test
  void testMatches_NullRaw_ReturnsFalse() {
    String encoded = PasswordEncoder.encode("password");
    assertFalse(PasswordEncoder.matches(encoded, null));
  }

  @Test
  void testMatches_InvalidFormat_ThrowsException() {
    assertThrows(RuntimeException.class, () ->
        PasswordEncoder.matches("invalid-no-at-sign", "password"));
  }
}
