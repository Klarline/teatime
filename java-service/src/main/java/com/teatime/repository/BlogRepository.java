package com.teatime.repository;

import com.teatime.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * JPA Repository for Blog entity
 */
public interface BlogRepository extends JpaRepository<Blog, Long> {

  Page<Blog> findByUserId(Long userId, Pageable pageable);

  Page<Blog> findAllByOrderByLikedDesc(Pageable pageable);

  @Modifying
  @Query("UPDATE Blog b SET b.liked = COALESCE(b.liked, 0) + 1 WHERE b.id = :id")
  int incrementLiked(@Param("id") Long id);

  @Modifying
  @Query("UPDATE Blog b SET b.liked = COALESCE(b.liked, 0) - 1 WHERE b.id = :id")
  int decrementLiked(@Param("id") Long id);
}
