package com.teatime.repository;

import com.teatime.entity.BlogComments;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA Repository for BlogComments entity
 */
public interface BlogCommentsRepository extends JpaRepository<BlogComments, Long> {
}
