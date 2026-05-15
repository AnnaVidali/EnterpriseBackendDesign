package com.application.enterprisebackenddesign.application.product;

import com.application.enterprisebackenddesign.api.product.ProductRequest;
import com.application.enterprisebackenddesign.application.shared.DomainEventPublisher;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private DomainEventPublisher eventPublisher;
    @InjectMocks
    private UpdateProductUseCase useCase;

    @Test
    void shouldUpdateAllFields() throws DomainException {
        Money price = new Money(BigDecimal.TEN, Currency.getInstance("USD"));
        Product existing = new Product(1L, "Widget", price, "WID-001");
        existing.pullEvents(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        ProductRequest request = new ProductRequest("Gadget", new BigDecimal("15.00"), "USD", "GAD-001");
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = useCase.update(1L, request);

        assertThat(result.getName()).isEqualTo("Gadget");
        assertThat(result.getPrice().getAmount()).isEqualByComparingTo(new BigDecimal("15.00"));
        assertThat(result.getSku()).isEqualTo("GAD-001");
        verify(eventPublisher, times(3)).publish(any());
    }

    @Test
    void shouldUpdateOnlyProvidedFields() throws DomainException {
        Money price = new Money(BigDecimal.TEN, Currency.getInstance("USD"));
        Product existing = new Product(1L, "Widget", price, "WID-001");
        existing.pullEvents(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        ProductRequest request = new ProductRequest("Gadget", null, null, null);
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = useCase.update(1L, request);

        assertThat(result.getName()).isEqualTo("Gadget");
        assertThat(result.getSku()).isEqualTo("WID-001");
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldThrowWhenProductNotFound() throws DomainException {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        ProductRequest request = new ProductRequest("Gadget", BigDecimal.TEN, "USD", "GAD-001");
        assertThatThrownBy(() -> useCase.update(99L, request))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Product not found");
        verify(productRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }
}
