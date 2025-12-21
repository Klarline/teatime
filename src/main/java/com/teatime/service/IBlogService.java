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

  Result queryHotBlog(Integer current);

  Result queryBlogById(Long id);

  Result likeBlog(Long id);

  Result queryBlogLikes(Long id);

  Result saveBlog(Blog blog);
}
