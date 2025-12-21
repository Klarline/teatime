package com.teatime.utils;

public class RedisConstants {
  public static final String LOGIN_CODE_KEY = "teatime:login:code:";
  public static final String LOGIN_USER_KEY = "teatime:login:token:";
  public static final String CACHE_SHOP_KEY = "teatime:cache:shop:";
  public static final String LOCK_SHOP_KEY = "teatime:lock:shop:";
  public static final String FLASH_SALE_STOCK_KEY = "teatime:flashsale:stock:";
  public static final String BLOG_LIKED_KEY = "teatime:blog:liked:";
  public static final String FEED_KEY = "teatime:feed:";
  public static final String SHOP_GEO_KEY = "teatime:shop:geo:";
  public static final String USER_CHECKIN_KEY = "teatime:user:checkin:";
  public static final String CACHE_SHOP_TYPE_KEY = "teatime:shop:type:list";

  public static final Long LOGIN_CODE_TTL = 2L;
  public static final Long LOGIN_USER_TTL = 36000L;
  public static final Long CACHE_NULL_TTL = 2L;
  public static final Long CACHE_SHOP_TTL = 30L;
  public static final Long LOCK_SHOP_TTL = 10L;
}
