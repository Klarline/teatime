package com.teatime.service;

import com.teatime.dto.Result;
import com.teatime.entity.Blog;

import java.util.List;

/**
 * <p>
 * Blog service interface
 * </p>
 */
public interface IBlogService {

  Result queryHotBlog(Integer current);

  Result queryBlogById(Long id);

  Result likeBlog(Long id);

  Result queryBlogLikes(Long id);

  Result saveBlog(Blog blog);

  Blog getById(Long id);

  List<Blog> queryBlogsByUserId(Long userId, Integer current);
}
