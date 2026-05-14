package com.application.enterprisebackenddesign.application.product;

import com.application.enterprisebackenddesign.domain.product.Product;
import com.application.enterprisebackenddesign.domain.product.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ListProductsUseCase {
    private final ProductRepository productRepository;

    public ListProductsUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> listAll() {
        return productRepository.findAll();
    }
}
