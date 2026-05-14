package com.application.enterprisebackenddesign.application.product;

import com.application.enterprisebackenddesign.domain.product.ProductRepository;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class DeleteProductUseCase {

    private final ProductRepository productRepository;

    public DeleteProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void delete(Long id) throws DomainException {
        if (productRepository.findById(id).isEmpty()) {
            throw new DomainException.ResourceNotFoundException("Product does not exist.");
        }
        productRepository.delete(productRepository.findById(id).get());
    }
}
