package com.application.enterprisebackenddesign.application.product;

import com.application.enterprisebackenddesign.domain.product.Product;
import com.application.enterprisebackenddesign.domain.product.ProductRepository;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class GetProductUseCase {

    private final ProductRepository productRepository;

    public GetProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product getProductById(Long id) throws DomainException {
        return productRepository.findById(id).orElseThrow(() -> new DomainException("Product not found"));
    }
}
