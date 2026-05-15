package com.application.enterprisebackenddesign.application.product;

import com.application.enterprisebackenddesign.api.product.ProductRequest;
import com.application.enterprisebackenddesign.application.shared.DomainEventPublisher;
import com.application.enterprisebackenddesign.domain.product.Product;
import com.application.enterprisebackenddesign.domain.product.ProductRepository;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Currency;

@Service
@Transactional
public class UpdateProductUseCase {
    private final ProductRepository productRepository;
    private final DomainEventPublisher eventPublisher;

    public UpdateProductUseCase(ProductRepository productRepository, DomainEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    public Product update(Long id, ProductRequest request) throws DomainException {
        Product product = productRepository.findById(id).orElseThrow(() -> new DomainException.ResourceNotFoundException("Product not found"));
        if (request.name() != null && !request.name().isEmpty()) {
            product.updateName(request.name());
        }
        if (request.price() != null) {
            Currency currency = Currency.getInstance(request.currency());
            Money price = new Money(request.price(), currency);
            product.updatePrice(price);
        }
        if (request.sku() != null && !request.sku().isEmpty()) {
            product.updateSku(request.sku());
        }
        Product updatedProduct = productRepository.save(product);
        product.pullEvents(true).forEach(eventPublisher::publish);
        return updatedProduct;
    }

}
