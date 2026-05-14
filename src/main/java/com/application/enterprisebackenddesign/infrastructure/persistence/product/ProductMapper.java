package com.application.enterprisebackenddesign.infrastructure.persistence.product;

import com.application.enterprisebackenddesign.domain.product.Product;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import com.application.enterprisebackenddesign.infrastructure.persistence.product.entity.ProductEntity;
import com.application.enterprisebackenddesign.infrastructure.persistence.shared.MoneyEmbeddable;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductEntity toEntity(Product product) throws DomainException {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setId(product.getId());
        productEntity.setName(product.getName());
        productEntity.setPrice(MoneyEmbeddable.fromDomain(product.getPrice()));
        productEntity.setSku(product.getSku());
        return productEntity;
    }

    public Product toDomain(ProductEntity productEntity) {
        try {
            Money price = productEntity.getPrice().toDomain();
            return new Product(productEntity.getId(), productEntity.getName(), price, productEntity.getSku());
        } catch (DomainException.BusinessRuleViolationException e) {
            throw new IllegalStateException("Corrupted product data in database", e);
        }
    }
}
