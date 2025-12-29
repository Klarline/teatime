package com.teatime.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teatime.dto.Result;
import com.teatime.entity.Blog;

/**
 * <p>
 * Blog service interface
 * </p>
 */
public interface IBlogService extends IService<Blog> {

  /**
   * Query hot blogs with pagination
   *
   * @param current Current page number
   * @return Result with hot blogs
   */
  Result queryHotBlog(Integer current);

  /**
   * Query blog by ID
   *
   * @param id Blog ID
   * @return Result with blog details
   */
  Result queryBlogById(Long id);

  /**
   * Like a blog
   *
   * @param id Blog ID
   * @return Result
   */
  Result likeBlog(Long id);

  /**
   * Query likes for a blog
   *
   * @param id Blog ID
   * @return Result with list of users who liked the blog
   */
  Result queryBlogLikes(Long id);

  /**
   * Save a new blog
   *
   * @param blog Blog object
   * @return Result with blog ID
   */
  Result saveBlog(Blog blog);
}
