package com.teatime.service.impl;

import static com.teatime.utils.RedisConstants.BLOG_LIKED_KEY;
import static com.teatime.utils.RedisConstants.FEED_KEY;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teatime.dto.Result;
import com.teatime.dto.UserDTO;
import com.teatime.dto.ai.ReviewDocument;
import com.teatime.entity.Blog;
import com.teatime.entity.Follow;
import com.teatime.entity.Shop;
import com.teatime.entity.User;
import com.teatime.mapper.BlogMapper;
import com.teatime.service.IAIService;
import com.teatime.service.IBlogService;
import com.teatime.service.IFollowService;
import com.teatime.service.IShopService;
import com.teatime.service.IUserService;
import com.teatime.utils.SystemConstants;
import com.teatime.utils.UserHolder;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Blog service implementation
 * </p>
 */
@Service
@RequiredArgsConstructor
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

  private final IUserService userService;
  private final StringRedisTemplate stringRedisTemplate;
  private final IFollowService followService;

  @Resource
  private IShopService shopService;

  @Resource
  private IAIService aiService;

  @Override
  public Result queryHotBlog(Integer current) {
    // query top liked blogs
    Page<Blog> page = query()
        .orderByDesc("liked")
        .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
    // get current page data
    List<Blog> records = page.getRecords();
    // query blog authors
    records.forEach(blog -> {
      queryBlogUser(blog);
      // check if current user liked the blog
      isBlogLiked(blog);
    });
    return Result.ok(records);
  }

  @Override
  public Result queryBlogById(Long id) {
    // query blog
    Blog blog = getById(id);
    if (blog == null) {
      return Result.fail("Blog does not exist");
    }
    // query blog author
    queryBlogUser(blog);

    // check if current user liked the blog
    isBlogLiked(blog);
    return Result.ok(blog);
  }

  private void isBlogLiked(Blog blog) {
    UserDTO user = UserHolder.getUser();
    if (user == null) {
      // if not logged in, cannot be liked
      return;
    }
    Long userId = user.getId();
    String key = "blog:liked:" + blog.getId();
    Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
    blog.setIsLike(score != null);
  }

  @Override
  public Result likeBlog(Long id) {
    Long userId = UserHolder.getUser().getId();

    // check if user has liked the blog
    String key = BLOG_LIKED_KEY + id;
    Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
    if (score == null) {
      boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
      if (isSuccess) {
        // add user to liked set
        stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
      }
    } else {
      // unlike the blog
      boolean isSuccess = update().setSql("liked = liked - 1").eq("id", id).update();
      if (isSuccess) {
        // remove user from liked set
        stringRedisTemplate.opsForZSet().remove(key, userId.toString());
      }
      return null;
    }
    return Result.ok();
  }

  @Override
  public Result queryBlogLikes(Long id) {
    String key = BLOG_LIKED_KEY + id;
    Set<String> top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);

    if (top5 == null || top5.isEmpty()) {
      return Result.ok(Collections.emptyList());
    }

    List<Long> ids = top5.stream().map(Long::valueOf).toList();
    String idStr = StrUtil.join(",", ids);
    List<UserDTO> userDTOs =
        userService.query()
            .in("id", ids)
            .last("ORDER BY FIELD(id," + idStr + ")").list()
            .stream()
            .map(user -> BeanUtil.copyProperties(user, UserDTO.class)).toList();
    return Result.ok(userDTOs);
  }

  @Override
  public Result saveBlog(Blog blog) {
    // get logged-in user
    UserDTO user = UserHolder.getUser();
    blog.setUserId(user.getId());
    // save blog to database
    boolean isSuccess = save(blog);
    if (!isSuccess) {
      return Result.fail("Failed to publish blog");
    }

    // async ingestion to AI service
    try {
      ReviewDocument review = new ReviewDocument();
      review.setReviewId(blog.getId());
      review.setShopId(blog.getShopId());
      review.setContent(blog.getContent());
      review.setTitle(blog.getTitle());

      // get shop name and username
      Shop shop = shopService.getById(blog.getShopId());
      if (shop != null) {
        review.setShopName(shop.getName());
      }
      review.setUserName(user.getNickName());

      aiService.ingestReview(review);
    } catch (Exception e) {
      log.error("Failed to ingest review to AI service", e);
    }

    // query followers of the author
    List<Follow> follows = followService.query().eq("followed_user_id", user.getId()).list();

    // push blog id to followers' feeds
    for (Follow follow : follows) {
      Long userId = follow.getUserId();
      String key = FEED_KEY + userId;
      stringRedisTemplate.opsForZSet()
          .add(key, blog.getId().toString(), System.currentTimeMillis());
    }

    // return blog id
    return Result.ok(blog.getId());
  }

  private void queryBlogUser(Blog blog) {
    Long userId = blog.getUserId();
    User user = userService.getById(userId);
    blog.setName(user.getNickName());
    blog.setIcon(user.getIcon());
  }
}
