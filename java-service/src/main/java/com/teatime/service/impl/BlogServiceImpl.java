package com.teatime.service.impl;

import static com.teatime.utils.RedisConstants.BLOG_LIKED_KEY;
import static com.teatime.utils.RedisConstants.FEED_KEY;

import cn.hutool.core.bean.BeanUtil;
import com.teatime.dto.Result;
import com.teatime.dto.UserDTO;
import com.teatime.dto.ai.ReviewDocument;
import com.teatime.entity.Blog;
import com.teatime.entity.Follow;
import com.teatime.entity.Shop;
import com.teatime.entity.User;
import com.teatime.repository.BlogRepository;
import com.teatime.service.IAIService;
import com.teatime.service.IBlogService;
import com.teatime.service.IFollowService;
import com.teatime.service.IShopService;
import com.teatime.service.IUserService;
import com.teatime.utils.RedisFallback;
import com.teatime.utils.SystemConstants;
import com.teatime.utils.UserHolder;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Blog service implementation
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements IBlogService {

  private final BlogRepository blogRepository;
  private final IUserService userService;
  private final StringRedisTemplate stringRedisTemplate;
  private final IFollowService followService;

  @Resource
  private IShopService shopService;

  @Resource
  private IAIService aiService;

  @Override
  public Result queryHotBlog(Integer current) {
    List<Blog> records = blogRepository.findAllByOrderByLikedDesc(
        PageRequest.of(current - 1, SystemConstants.MAX_PAGE_SIZE)
    ).getContent();

    records.forEach(blog -> {
      queryBlogUser(blog);
      isBlogLiked(blog);
    });
    return Result.ok(records);
  }

  @Override
  public Result queryBlogById(Long id) {
    Blog blog = blogRepository.findById(id).orElse(null);
    if (blog == null) {
      return Result.fail("Blog does not exist");
    }
    queryBlogUser(blog);
    isBlogLiked(blog);
    return Result.ok(blog);
  }

  private void isBlogLiked(Blog blog) {
    UserDTO user = UserHolder.getUser();
    if (user == null) {
      return;
    }
    Long userId = user.getId();
    String key = BLOG_LIKED_KEY + blog.getId();
    Double score = RedisFallback.execute(
        () -> stringRedisTemplate.opsForZSet().score(key, userId.toString()),
        () -> null
    );
    blog.setIsLike(score != null);
  }

  @Override
  public Result likeBlog(Long id) {
    Long userId = UserHolder.getUser().getId();
    String key = BLOG_LIKED_KEY + id;
    Double score = RedisFallback.execute(
        () -> stringRedisTemplate.opsForZSet().score(key, userId.toString()),
        () -> null
    );
    if (score == null) {
      int updated = blogRepository.incrementLiked(id);
      if (updated > 0) {
        RedisFallback.executeVoid(() ->
            stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis()));
      }
    } else {
      int updated = blogRepository.decrementLiked(id);
      if (updated > 0) {
        RedisFallback.executeVoid(() ->
            stringRedisTemplate.opsForZSet().remove(key, userId.toString()));
      }
      return null;
    }
    return Result.ok();
  }

  @Override
  public Result queryBlogLikes(Long id) {
    String key = BLOG_LIKED_KEY + id;
    Set<String> top5 = RedisFallback.execute(
        () -> stringRedisTemplate.opsForZSet().range(key, 0, 4),
        () -> Collections.emptySet()
    );

    if (top5 == null || top5.isEmpty()) {
      return Result.ok(Collections.emptyList());
    }

    List<Long> ids = top5.stream().map(Long::valueOf).toList();
    List<User> users = userService.listByIds(ids);
    users.sort(Comparator.comparingInt(u -> ids.indexOf(u.getId())));
    List<UserDTO> userDTOs = users.stream()
        .map(user -> BeanUtil.copyProperties(user, UserDTO.class)).toList();
    return Result.ok(userDTOs);
  }

  @Override
  public Result saveBlog(Blog blog) {
    UserDTO user = UserHolder.getUser();
    blog.setUserId(user.getId());
    Blog saved = blogRepository.save(blog);
    if (saved == null) {
      return Result.fail("Failed to publish blog");
    }

    try {
      ReviewDocument review = new ReviewDocument();
      review.setReviewId(blog.getId());
      review.setShopId(blog.getShopId());
      review.setContent(blog.getContent());
      review.setTitle(blog.getTitle());

      Shop shop = shopService.getById(blog.getShopId());
      if (shop != null) {
        review.setShopName(shop.getName());
      }
      review.setUserName(user.getNickName());

      aiService.ingestReview(review);
    } catch (Exception e) {
      log.error("Failed to ingest review to AI service", e);
    }

    List<Follow> follows = followService.findByFollowUserId(user.getId());
    for (Follow follow : follows) {
      Long followerId = follow.getUserId();
      String feedKey = FEED_KEY + followerId;
      RedisFallback.executeVoid(() ->
          stringRedisTemplate.opsForZSet()
              .add(feedKey, blog.getId().toString(), System.currentTimeMillis()));
    }

    return Result.ok(blog.getId());
  }

  @Override
  public Blog getById(Long id) {
    return blogRepository.findById(id).orElse(null);
  }

  @Override
  public List<Blog> queryBlogsByUserId(Long userId, Integer current) {
    return blogRepository.findByUserId(
        userId,
        PageRequest.of(current - 1, SystemConstants.MAX_PAGE_SIZE)
    ).getContent();
  }

  private void queryBlogUser(Blog blog) {
    Long userId = blog.getUserId();
    User user = userService.getById(userId);
    if (user != null) {
      blog.setName(user.getNickName());
      blog.setIcon(user.getIcon());
    }
  }
}
