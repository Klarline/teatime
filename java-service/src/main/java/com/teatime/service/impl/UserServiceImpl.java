package com.teatime.service.impl;

import static com.teatime.utils.RedisConstants.LOGIN_CODE_KEY;
import static com.teatime.utils.RedisConstants.LOGIN_CODE_TTL;
import static com.teatime.utils.RedisConstants.LOGIN_USER_KEY;
import static com.teatime.utils.RedisConstants.LOGIN_USER_TTL;
import static com.teatime.utils.RedisConstants.USER_CHECKIN_KEY;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teatime.dto.LoginFormDTO;
import com.teatime.dto.Result;
import com.teatime.dto.UserDTO;
import com.teatime.entity.User;
import com.teatime.mapper.UserMapper;
import com.teatime.service.IUserService;
import com.teatime.utils.RegexUtils;
import com.teatime.utils.UserHolder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Service implementation class for user-related operations
 * </p>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

  @Resource
  private StringRedisTemplate stringRedisTemplate;

  @Override
  public Result sendCode(String phone, HttpSession session) {
    // validate phone number format
    if (RegexUtils.isPhoneInvalid(phone)) {
      // if invalid, return error result
      return Result.fail("Phone number format is invalid");
    }

    // if valid, generate verification code
    String code = RandomUtil.randomString(6);

    // save the code to Redis with a 2-minute expiration time
    stringRedisTemplate.opsForValue()
        .set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);

    // simulate sending the code via SMS
    log.debug("Sending verification code: " + code);

    // return success result
    return Result.ok();
  }

  @Override
  public Result login(LoginFormDTO loginForm, HttpSession session) {
    // validate phone number format
    String phone = loginForm.getPhone();
    if (RegexUtils.isPhoneInvalid(phone)) {
      // if invalid, return error result
      return Result.fail("Phone number format is invalid");
    }

    // validate verification code format and correctness
    Object cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
    String code = loginForm.getCode();
    if (cacheCode == null || !cacheCode.toString().equals(code)) {
      // if invalid, return error result
      return Result.fail("Verification code is incorrect");
    }

    // check if user exists
    User user = query().eq("phone", phone).one();

    // create new user if not exists
    if (user == null) {
      user = createUserWithPhone(phone);
    }

    // save user info to Redis
    // generate unique token for user session
    String token = UUID.randomUUID().toString(true);
    UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
    Map<String, Object> userMap =
        BeanUtil.beanToMap(userDTO, new HashMap<>(),
            CopyOptions.create().setIgnoreNullValue(true)
                .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
    stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY + token, userMap);
    // set expiration time for user session
    stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.SECONDS);

    return Result.ok(token);
  }

  @Override
  public Result checkIn() {
    // get current logged-in user
    UserDTO userId = UserHolder.getUser();
    // get current date
    LocalDateTime now = LocalDateTime.now();
    String keySuffix = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
    String key = USER_CHECKIN_KEY + userId + keySuffix;
    // get day of month
    int dayOfMonth = now.getDayOfMonth();
    // set the bit at the position of the current day to 1
    stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
    return Result.ok();
  }

  @Override
  public Result checkInCount() {
    // get current logged-in user
    UserDTO userId = UserHolder.getUser();
    // get current date
    LocalDateTime now = LocalDateTime.now();
    String keySuffix = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
    String key = USER_CHECKIN_KEY + userId + keySuffix;
    // get day of month
    int dayOfMonth = now.getDayOfMonth();

    // get check-in records up to today
    List<Long> result = stringRedisTemplate.opsForValue()
        .bitField(key, BitFieldSubCommands.create()
            .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0));
    if (result == null || result.isEmpty()) {
      return Result.ok(0);
    }

    Long num = result.get(0);
    if (num == null || num == 0) {
      return Result.ok(0);
    }

    // count consecutive check-ins
    int dayCount = 0;
    while (true) {
      // check if the last bit is 0
      if ((num & 1) == 0) {
        break;
      } else {
        dayCount++;
        num >>>= 1;
      }
    }

    return Result.ok(dayCount);
  }

  private User createUserWithPhone(String phone) {
    User user = new User();
    user.setPhone(phone);
    user.setNickName("USER_" + RandomUtil.randomString(10));
    save(user);
    return user;
  }
}
