package com.application.enterprisebackenddesign.api.product;

import com.application.enterprisebackenddesign.domain.product.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice().getAmount(),
                product.getPrice().getCurrency().getCurrencyCode(),
                product.getSku()
        );
    }
}
