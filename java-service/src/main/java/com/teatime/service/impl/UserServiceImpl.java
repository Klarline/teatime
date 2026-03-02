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
import com.teatime.dto.LoginFormDTO;
import com.teatime.dto.Result;
import com.teatime.dto.UserDTO;
import com.teatime.entity.User;
import com.teatime.repository.UserRepository;
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
public class UserServiceImpl implements IUserService {

  @Resource
  private UserRepository userRepository;

  @Resource
  private StringRedisTemplate stringRedisTemplate;

  @Override
  public Result sendCode(String phone, HttpSession session) {
    if (RegexUtils.isPhoneInvalid(phone)) {
      return Result.fail("Phone number format is invalid");
    }

    String code = RandomUtil.randomString(6);
    stringRedisTemplate.opsForValue()
        .set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
    return Result.ok();
  }

  @Override
  public Result login(LoginFormDTO loginForm, HttpSession session) {
    String phone = loginForm.getPhone();
    if (RegexUtils.isPhoneInvalid(phone)) {
      return Result.fail("Phone number format is invalid");
    }

    Object cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
    String code = loginForm.getCode();
    if (cacheCode == null || !cacheCode.toString().equals(code)) {
      return Result.fail("Verification code is incorrect");
    }

    User user = userRepository.findByPhone(phone).orElse(null);
    if (user == null) {
      user = createUserWithPhone(phone);
    }

    String token = UUID.randomUUID().toString(true);
    UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
    Map<String, Object> userMap =
        BeanUtil.beanToMap(userDTO, new HashMap<>(),
            CopyOptions.create().setIgnoreNullValue(true)
                .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
    stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY + token, userMap);
    stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.SECONDS);

    return Result.ok(token);
  }

  @Override
  public Result checkIn() {
    UserDTO user = UserHolder.getUser();
    LocalDateTime now = LocalDateTime.now();
    String keySuffix = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
    String key = USER_CHECKIN_KEY + user.getId() + keySuffix;
    int dayOfMonth = now.getDayOfMonth();
    stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
    return Result.ok();
  }

  @Override
  public Result checkInCount() {
    UserDTO user = UserHolder.getUser();
    LocalDateTime now = LocalDateTime.now();
    String keySuffix = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
    String key = USER_CHECKIN_KEY + user.getId() + keySuffix;
    int dayOfMonth = now.getDayOfMonth();

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

    int dayCount = 0;
    while (true) {
      if ((num & 1) == 0) {
        break;
      } else {
        dayCount++;
        num >>>= 1;
      }
    }

    return Result.ok(dayCount);
  }

  @Override
  public User getById(Long id) {
    return userRepository.findById(id).orElse(null);
  }

  @Override
  public List<User> listByIds(List<Long> ids) {
    return userRepository.findAllById(ids);
  }

  private User createUserWithPhone(String phone) {
    User user = new User();
    user.setPhone(phone);
    user.setNickName("USER_" + RandomUtil.randomString(10));
    userRepository.save(user);
    return user;
  }
}
