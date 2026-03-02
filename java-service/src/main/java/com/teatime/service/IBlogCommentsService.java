package com.teatime.service;

import com.teatime.entity.BlogComments;

/**
 * <p>
 * Blog Comments service interface
 * </p>
 */
public interface IBlogCommentsService {

  BlogComments getById(Long id);

  BlogComments save(BlogComments blogComments);
}
