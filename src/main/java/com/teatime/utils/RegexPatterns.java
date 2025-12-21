package com.teatime.utils;

public abstract class RegexPatterns {
  /**
   * Phone number regex
   */
  public static final String PHONE_REGEX = "^\\+?1?[2-9]\\d{2}[2-9]\\d{6}$";
  /**
   * Email regex
   */
  public static final String EMAIL_REGEX = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
  /**
   * Password regex, 4-32 word characters
   */
  public static final String PASSWORD_REGEX = "^\\w{4,32}$";
  /**
   * Verification code regex, 6 alphanumeric characters
   */
  public static final String VERIFY_CODE_REGEX = "^[a-zA-Z\\d]{6}$";

}
