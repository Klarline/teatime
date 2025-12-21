package com.teatime.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teatime.entity.BlogComments;
import com.teatime.mapper.BlogCommentsMapper;
import com.teatime.service.IBlogCommentsService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Blog comments service implementation
 * </p>
 */
@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments>
    implements IBlogCommentsService {

}
