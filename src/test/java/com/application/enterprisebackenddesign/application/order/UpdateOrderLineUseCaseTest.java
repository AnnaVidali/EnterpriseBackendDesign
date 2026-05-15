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
class UpdateOrderLineUseCaseTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private DomainEventPublisher eventPublisher;
    @InjectMocks
    private UpdateOrderLineUseCase useCase;

    @Test
    void shouldUpdateOrderLineQuantityAndPublishEvents() throws DomainException {
        Money price = new Money(BigDecimal.TEN, USD);
        OrderLine line = new OrderLine(1L, 10L, 2, price);
        Order order = new Order(1L, 1L, List.of(line), USD);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = useCase.execute(1L, 1L, 5);

        assertThat(result.getOrderLines()).hasSize(1);
        assertThat(result.getOrderLines().get(0).getQuantity()).isEqualTo(5);
        verify(orderRepository).save(order);
        verify(eventPublisher, atLeastOnce()).publish(any());
    }

    @Test
    void shouldRemoveOrderLineWhenQuantityIsZero() throws DomainException {
        Money price = new Money(BigDecimal.TEN, USD);
        OrderLine line = new OrderLine(1L, 10L, 2, price);
        Order order = new Order(1L, 1L, List.of(line), USD);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = useCase.execute(1L, 1L, 0);

        assertThat(result.getOrderLines()).isEmpty();
    }

    @Test
    void shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L, 1L, 5))
                .isInstanceOf(DomainException.ResourceNotFoundException.class);
        verify(orderRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldThrowWhenOrderLineNotFound() throws DomainException {
        Money price = new Money(BigDecimal.TEN, USD);
        OrderLine line = new OrderLine(1L, 10L, 2, price);
        Order order = new Order(1L, 1L, List.of(line), USD);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> useCase.execute(1L, 99L, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OrderLine not found");
        verify(orderRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldThrowWhenDomainValidationFails() throws DomainException {
        Money price = new Money(BigDecimal.TEN, USD);
        OrderLine line = new OrderLine(1L, 10L, 2, price);
        Order order = new Order(1L, 1L, List.of(line), USD);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> useCase.execute(1L, 1L, -1))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("cannot be negative");
        verify(orderRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }
}
