package com.teatime.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.teatime.entity.BlogComments;
import com.teatime.repository.BlogCommentsRepository;
import com.teatime.service.impl.BlogCommentsServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BlogCommentsServiceTest {

  @Mock
  private BlogCommentsRepository blogCommentsRepository;

  @InjectMocks
  private BlogCommentsServiceImpl blogCommentsService;

  @Test
  void testGetById_Exists_ReturnsComment() {
    BlogComments comment = new BlogComments();
    comment.setId(1L);
    comment.setContent("Great post!");
    when(blogCommentsRepository.findById(1L)).thenReturn(Optional.of(comment));

    BlogComments result = blogCommentsService.getById(1L);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("Great post!", result.getContent());
  }

  @Test
  void testGetById_NotExists_ReturnsNull() {
    when(blogCommentsRepository.findById(999L)).thenReturn(Optional.empty());

    BlogComments result = blogCommentsService.getById(999L);

    assertNull(result);
  }

  @Test
  void testSave_ReturnsSavedComment() {
    BlogComments comment = new BlogComments();
    comment.setContent("New comment");
    BlogComments saved = new BlogComments();
    saved.setId(1L);
    saved.setContent("New comment");
    when(blogCommentsRepository.save(any(BlogComments.class))).thenReturn(saved);

    BlogComments result = blogCommentsService.save(comment);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    verify(blogCommentsRepository).save(comment);
  }
}
