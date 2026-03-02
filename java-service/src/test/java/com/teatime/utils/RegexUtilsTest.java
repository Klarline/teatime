package com.teatime.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RegexUtilsTest {

  @Test
  void testIsPhoneInvalid_ValidCanadianPhone_ReturnsFalse() {
    assertFalse(RegexUtils.isPhoneInvalid("6045551234"));
    assertFalse(RegexUtils.isPhoneInvalid("+16045551234"));
  }

  @Test
  void testIsPhoneInvalid_InvalidPhone_ReturnsTrue() {
    assertTrue(RegexUtils.isPhoneInvalid("123"));
    assertTrue(RegexUtils.isPhoneInvalid(""));
    assertTrue(RegexUtils.isPhoneInvalid("   "));
    assertTrue(RegexUtils.isPhoneInvalid(null));
  }

  @Test
  void testIsEmailInvalid_ValidEmail_ReturnsFalse() {
    assertFalse(RegexUtils.isEmailInvalid("user@example.com"));
    assertFalse(RegexUtils.isEmailInvalid("test_user@domain.co.uk"));
  }

  @Test
  void testIsEmailInvalid_InvalidEmail_ReturnsTrue() {
    assertTrue(RegexUtils.isEmailInvalid("invalid"));
    assertTrue(RegexUtils.isEmailInvalid("invalid@"));
    assertTrue(RegexUtils.isEmailInvalid(""));
    assertTrue(RegexUtils.isEmailInvalid(null));
  }

  @Test
  void testIsCodeInvalid_ValidCode_ReturnsFalse() {
    assertFalse(RegexUtils.isCodeInvalid("123456"));
    assertFalse(RegexUtils.isCodeInvalid("abc123"));
  }

  @Test
  void testIsCodeInvalid_InvalidCode_ReturnsTrue() {
    assertTrue(RegexUtils.isCodeInvalid("12345"));
    assertTrue(RegexUtils.isCodeInvalid("1234567"));
    assertTrue(RegexUtils.isCodeInvalid(""));
    assertTrue(RegexUtils.isCodeInvalid(null));
  }
}
