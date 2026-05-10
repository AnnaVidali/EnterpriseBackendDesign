package com.application.enterprisebackenddesign.domain.product;

import com.application.enterprisebackenddesign.domain.shared.DomainException;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    public List<Product> findAll();

    public Optional<Product> findById(Long id);

    public Product save(Product product) throws DomainException;

    public Optional<Product> findBySku(String sku);

    public void delete(Product product);
}
