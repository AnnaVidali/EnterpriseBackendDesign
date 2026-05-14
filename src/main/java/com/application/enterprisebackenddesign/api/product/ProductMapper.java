package com.application.enterprisebackenddesign.api.product;

import com.application.enterprisebackenddesign.domain.product.Product;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import org.springframework.stereotype.Component;

import java.util.Currency;

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

    public Money toMoney(ProductRequest request) throws DomainException {
        return new Money(request.price(), Currency.getInstance(request.currency()));
    }
}
