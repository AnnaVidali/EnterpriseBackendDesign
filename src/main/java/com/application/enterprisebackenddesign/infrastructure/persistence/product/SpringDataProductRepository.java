package com.application.enterprisebackenddesign.infrastructure.persistence.product;

import com.application.enterprisebackenddesign.infrastructure.persistence.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataProductRepository extends JpaRepository<ProductEntity, Long> {

    Optional<ProductEntity> findBySku(String sku);
}
