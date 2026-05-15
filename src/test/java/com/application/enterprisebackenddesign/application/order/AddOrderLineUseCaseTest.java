package com.application.enterprisebackenddesign.application.order;

import com.application.enterprisebackenddesign.application.shared.DomainEventPublisher;
import com.application.enterprisebackenddesign.domain.order.Order;
import com.application.enterprisebackenddesign.domain.order.OrderLine;
import com.application.enterprisebackenddesign.domain.order.OrderRepository;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddOrderLineUseCaseTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private DomainEventPublisher eventPublisher;
    @InjectMocks
    private AddOrderLineUseCase useCase;

    @Test
    void shouldAddOrderLineAndPublishEvents() throws DomainException {
        Money price = new Money(BigDecimal.TEN, USD);
        OrderLine existingLine = new OrderLine(1L, 10L, 2, price);
        Order order = new Order(1L, 1L, List.of(existingLine), USD);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = useCase.execute(1L, 2L, 20L, price, 3);

        assertThat(result.getOrderLines()).hasSize(2);
        verify(orderRepository).save(order);
        verify(eventPublisher, atLeastOnce()).publish(any());
    }

    @Test
    void shouldThrowWhenOrderNotFound() throws DomainException {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        Money price = new Money(BigDecimal.TEN, USD);
        assertThatThrownBy(() -> useCase.execute(99L, 2L, 20L, price, 3))
                .isInstanceOf(DomainException.ResourceNotFoundException.class);
        verify(orderRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldThrowWhenDomainValidationFails() throws DomainException {
        Money price = new Money(BigDecimal.TEN, USD);
        OrderLine line = new OrderLine(1L, 10L, 2, price);
        Order order = new Order(1L, 1L, List.of(line), USD);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> useCase.execute(1L, 1L, 20L, price, 3))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("already exists");
        verify(orderRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }
}
