package com.teatime.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teatime.dto.Result;
import com.teatime.dto.UserDTO;
import com.teatime.entity.Blog;
import com.teatime.service.IBlogService;
import com.teatime.utils.SystemConstants;
import com.teatime.utils.UserHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * Front-end controller for blog-related operations
 * </p>
 */
@RestController
@RequestMapping("/api/blog")
public class BlogController {

  @Resource
  private IBlogService blogService;

  /**
   * Create a new blog post.
   * POST /api/blog
   * Body: { "content": "My first blog post", "images": [...] }
   */
  @PostMapping
  public Result saveBlog(@RequestBody Blog blog) {
    return blogService.saveBlog(blog);
  }

  /**
   * Like a blog post
   * PUT /api/blog/like/{id}
   */
  @PutMapping("/like/{id}")
  public Result likeBlog(@PathVariable("id") Long id) {
    return blogService.likeBlog(id);
  }

  /**
   * Get blogs of the logged-in user
   * GET /api/blog/of/me?current=1
   */
  @GetMapping("/of/me")
  public Result queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
    // get logged-in user
    UserDTO user = UserHolder.getUser();
    // query blogs by user id
    Page<Blog> page = blogService.query()
        .eq("user_id", user.getId()).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
    // get current page data
    List<Blog> records = page.getRecords();
    return Result.ok(records);
  }

  /**
   * Get hot blogs
   * GET /api/blog/hot?current=1
   */
  @GetMapping("/hot")
  public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
    return blogService.queryHotBlog(current);
  }

  /**
   * Get blog details by ID
   * GET /api/blog/{id}
   */
  @GetMapping("/{id}")
  public Result queryBlogById(@PathVariable("id") Long id) {
    return blogService.queryBlogById(id);
  }

  /**
   * Get users who liked a blog post
   * GET /api/blog/likes/{id}
   */
  @GetMapping("/likes/{id}")
  public Result queryBlogLikes(@PathVariable("id") Long id) {
    return blogService.queryBlogLikes(id);
  }

  /**
   * Get blogs by a specific user
   * GET /api/blog/of/user?id=1&current=1
   */
  @GetMapping
  public Result queryBlogByUserId(
      @RequestParam("id") Long userId,
      @RequestParam(value = "current", defaultValue = "1") Integer current) {
    // query blogs by user id
    Page<Blog> page = blogService.query()
        .eq("user_id", userId).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));

    // get current page data
    List<Blog> records = page.getRecords();
    return Result.ok(records);
  }
}
