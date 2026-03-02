package com.teatime.repository;

import com.teatime.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * JPA Repository for Follow entity
 */
public interface FollowRepository extends JpaRepository<Follow, Long> {

  long countByUserIdAndFollowUserId(Long userId, Long followUserId);

  long countByFollowUserId(Long followUserId);

  long countByUserId(Long userId);

  boolean existsByUserIdAndFollowUserId(Long userId, Long followUserId);

  void deleteByUserIdAndFollowUserId(Long userId, Long followUserId);

  List<Follow> findByFollowUserId(Long followUserId);
}
