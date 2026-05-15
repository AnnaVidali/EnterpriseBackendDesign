package com.application.enterprisebackenddesign.infrastructure.persistence.product;

import com.application.enterprisebackenddesign.domain.product.Product;
import com.application.enterprisebackenddesign.domain.product.ProductRepository;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.infrastructure.persistence.product.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepositoryImpl implements ProductRepository {
    private final SpringDataProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductRepositoryImpl(SpringDataProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll().stream()
                .map(productMapper::toDomain)
                .toList();
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toDomain);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toDomain);
    }

    @Override
    public Product save(Product product) throws DomainException {
        ProductEntity productEntity = productMapper.toEntity(product);
        ProductEntity saved = productRepository.save(productEntity);
        return productMapper.toDomain(saved);
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return productRepository.findBySku(sku)
                .map(productMapper::toDomain);
    }

    @Override
    public void delete(Product product) {
        productRepository.deleteById(product.getId());
    }
}
