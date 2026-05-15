package com.application.enterprisebackenddesign.application.product;

import com.application.enterprisebackenddesign.domain.product.Product;
import com.application.enterprisebackenddesign.domain.product.ProductRepository;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListProductsUseCaseTest {

    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private ListProductsUseCase useCase;

    @Test
    void shouldReturnAllProducts() throws DomainException {
        Money price = new Money(BigDecimal.TEN, Currency.getInstance("USD"));
        List<Product> products = List.of(new Product(1L, "Widget", price, "WID-001"));
        when(productRepository.findAll()).thenReturn(products);

        List<Product> result = useCase.listAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldReturnPagedProducts() throws DomainException {
        Money price = new Money(BigDecimal.TEN, Currency.getInstance("USD"));
        List<Product> products = List.of(new Product(1L, "Widget", price, "WID-001"));
        Pageable pageable = PageRequest.of(0, 20);
        when(productRepository.findAll(pageable)).thenReturn(new PageImpl<>(products, pageable, 1));

        Page<Product> result = useCase.listAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}
