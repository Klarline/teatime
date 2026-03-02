package com.teatime.repository;

import com.teatime.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * JPA Repository for User entity
 */
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByPhone(String phone);
}
