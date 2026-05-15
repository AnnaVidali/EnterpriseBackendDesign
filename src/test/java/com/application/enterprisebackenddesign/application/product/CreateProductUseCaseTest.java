package com.application.enterprisebackenddesign.application.product;

import com.application.enterprisebackenddesign.api.product.ProductRequest;
import com.application.enterprisebackenddesign.application.shared.DomainEventPublisher;
import com.application.enterprisebackenddesign.domain.product.Product;
import com.application.enterprisebackenddesign.domain.product.ProductRepository;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
class CreateProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private DomainEventPublisher eventPublisher;
    @InjectMocks
    private CreateProductUseCase useCase;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    @Test
    void shouldCreateProductAndPublishEvents() throws DomainException {
        ProductRequest request = new ProductRequest("Widget", new BigDecimal("19.99"), "USD", "WID-001");
        when(productRepository.findBySku("WID-001")).thenReturn(Optional.empty());
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = useCase.create(1L, request);

        assertThat(result.getName()).isEqualTo("Widget");
        assertThat(result.getSku()).isEqualTo("WID-001");
        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().getName()).isEqualTo("Widget");
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldThrowWhenSkuNotUnique() throws DomainException {
        ProductRequest request = new ProductRequest("Widget", new BigDecimal("19.99"), "USD", "WID-001");
        when(productRepository.findBySku("WID-001")).thenReturn(Optional.of(mock(Product.class)));

        assertThatThrownBy(() -> useCase.create(1L, request))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("SKU is not unique");
        verify(productRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldThrowWhenDomainValidationFails() throws DomainException {
        ProductRequest request = new ProductRequest("", new BigDecimal("19.99"), "USD", "WID-001");
        when(productRepository.findBySku("WID-001")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.create(1L, request))
                .isInstanceOf(DomainException.class);
        verify(productRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }
}
