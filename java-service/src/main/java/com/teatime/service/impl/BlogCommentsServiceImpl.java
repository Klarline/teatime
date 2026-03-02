package com.teatime.service.impl;

import com.teatime.entity.BlogComments;
import com.teatime.repository.BlogCommentsRepository;
import com.teatime.service.IBlogCommentsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * Blog Comments service implementation
 * </p>
 */
@Service
public class BlogCommentsServiceImpl implements IBlogCommentsService {

  @Resource
  private BlogCommentsRepository blogCommentsRepository;

  @Override
  public BlogComments getById(Long id) {
    return blogCommentsRepository.findById(id).orElse(null);
  }

  @Override
  public BlogComments save(BlogComments blogComments) {
    return blogCommentsRepository.save(blogComments);
  }
}
