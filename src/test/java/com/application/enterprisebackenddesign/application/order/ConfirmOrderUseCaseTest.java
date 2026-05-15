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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmOrderUseCaseTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private DomainEventPublisher eventPublisher;
    @InjectMocks
    private ConfirmOrderUseCase useCase;

    @Test
    void shouldConfirmOrderAndPublishEvents() throws DomainException {
        Money price = new Money(BigDecimal.TEN, USD);
        OrderLine line = new OrderLine(1L, 10L, 2, price);
        Order order = new Order(1L, 1L, List.of(line), USD);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        Order result = useCase.execute(1L);

        assertThat(result.getStatus().name()).isEqualTo("CONFIRMED");
        verify(orderRepository).save(order);
        verify(eventPublisher, atLeastOnce()).publish(any());
    }

    @Test
    void shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(99L))
                .isInstanceOf(DomainException.ResourceNotFoundException.class);
        verify(orderRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }
}
