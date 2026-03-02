package com.teatime.utils;


import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

public class PasswordEncoder {

  public static String encode(String password) {
    // generate salt
    String salt = RandomStringUtils.randomAlphanumeric(20);
    // encode password with salt
    return encode(password, salt);
  }

  private static String encode(String password, String salt) {
    // encode password with salt
    return salt + "@" +
        DigestUtils.md5DigestAsHex((password + salt).getBytes(StandardCharsets.UTF_8));
  }

  public static Boolean matches(String encodedPassword, String rawPassword) {
    if (encodedPassword == null || rawPassword == null) {
      return false;
    }
    if (!encodedPassword.contains("@")) {
      throw new RuntimeException("Invalid encoded password format");
    }
    String[] arr = encodedPassword.split("@");
    // get salt
    String salt = arr[0];
    // compare encoded password
    return encodedPassword.equals(encode(rawPassword, salt));
  }
}
