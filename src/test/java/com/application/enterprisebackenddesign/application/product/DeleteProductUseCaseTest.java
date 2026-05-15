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

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private DeleteProductUseCase useCase;

    @Test
    void shouldDeleteExistingProduct() throws DomainException {
        Money price = new Money(BigDecimal.TEN, Currency.getInstance("USD"));
        Product product = new Product(1L, "Widget", price, "WID-001");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        useCase.delete(1L);

        verify(productRepository).delete(product);
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.delete(99L))
                .isInstanceOf(DomainException.ResourceNotFoundException.class)
                .hasMessageContaining("Product does not exist");
        verify(productRepository, never()).delete(any());
    }
}
