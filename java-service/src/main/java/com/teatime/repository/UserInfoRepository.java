package com.teatime.repository;

import com.teatime.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA Repository for UserInfo entity
 */
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
}
