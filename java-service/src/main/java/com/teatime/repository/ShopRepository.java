package com.teatime.repository;

import com.teatime.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * JPA Repository for Shop entity
 */
public interface ShopRepository extends JpaRepository<Shop, Long> {

  Page<Shop> findByTypeId(Long typeId, Pageable pageable);

  Page<Shop> findByNameContaining(String name, Pageable pageable);

  List<Shop> findByIdIn(List<Long> ids);
}
