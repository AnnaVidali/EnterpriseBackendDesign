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

/**
 * Use case for creating a new product. Validates SKU uniqueness,
 * constructs the product aggregate, persists it, and publishes
 * domain events.
 */
@Service
@Transactional
public class CreateProductUseCase {

    private final ProductRepository productRepository;
    private final DomainEventPublisher eventPublisher;

    public CreateProductUseCase(ProductRepository productRepository, DomainEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    public Product create(Long id, ProductRequest request) throws DomainException {
        if (productRepository.findBySku(request.sku()).isPresent()) {
            throw new DomainException.BusinessRuleViolationException("SKU is not unique.");
        }

        Currency currency = Currency.getInstance(request.currency());
        Money price = new Money(request.price(), currency);

        Product product = new Product(id, request.name(), price, request.sku());

        Product savedProduct = productRepository.save(product);

        product.pullEvents(true).forEach(eventPublisher::publish);

        return savedProduct;
    }
}
