package com.application.enterprisebackenddesign.domain.product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    public List<Product> findAll();

    public Optional<Product> findById(Long id);

    public Product save(Product product);

    public Optional<Product> findBySku(String sku);

    public Product update(Product product);

    public void delete(Product product);
}
