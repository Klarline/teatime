package com.teatime.controller;


import com.teatime.dto.Result;
import com.teatime.dto.UserDTO;
import com.teatime.entity.Blog;
import com.teatime.service.IBlogService;
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

  @PostMapping
  public Result saveBlog(@RequestBody Blog blog) {
    return blogService.saveBlog(blog);
  }

  @PutMapping("/like/{id}")
  public Result likeBlog(@PathVariable("id") Long id) {
    return blogService.likeBlog(id);
  }

  @GetMapping("/of/me")
  public Result queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
    UserDTO user = UserHolder.getUser();
    List<Blog> records = blogService.queryBlogsByUserId(user.getId(), current);
    return Result.ok(records);
  }

  @GetMapping("/hot")
  public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
    return blogService.queryHotBlog(current);
  }

  @GetMapping("/{id}")
  public Result queryBlogById(@PathVariable("id") Long id) {
    return blogService.queryBlogById(id);
  }

  @GetMapping("/likes/{id}")
  public Result queryBlogLikes(@PathVariable("id") Long id) {
    return blogService.queryBlogLikes(id);
  }

  @GetMapping
  public Result queryBlogByUserId(
      @RequestParam("id") Long userId,
      @RequestParam(value = "current", defaultValue = "1") Integer current) {
    List<Blog> records = blogService.queryBlogsByUserId(userId, current);
    return Result.ok(records);
  }
}
