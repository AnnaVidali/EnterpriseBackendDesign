package com.application.enterprisebackenddesign.domain.product;

import com.application.enterprisebackenddesign.domain.shared.DomainException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    List<Product> findAll();

    Page<Product> findAll(Pageable pageable);

    Optional<Product> findById(Long id);

    Product save(Product product) throws DomainException;

    Optional<Product> findBySku(String sku);

    void delete(Product product);
}
