package com.teatime.repository;

import com.teatime.entity.ShopType;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA Repository for ShopType entity
 */
public interface ShopTypeRepository extends JpaRepository<ShopType, Long> {
}
