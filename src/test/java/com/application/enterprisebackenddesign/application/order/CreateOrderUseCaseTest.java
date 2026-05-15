package com.application.enterprisebackenddesign.application.order;

import com.application.enterprisebackenddesign.application.shared.DomainEventPublisher;
import com.application.enterprisebackenddesign.domain.order.Order;
import com.application.enterprisebackenddesign.domain.order.OrderLine;
import com.application.enterprisebackenddesign.domain.order.OrderRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private DomainEventPublisher eventPublisher;
    @InjectMocks
    private CreateOrderUseCase useCase;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    @Test
    void shouldCreateOrderAndPublishEvents() throws DomainException {
        Money price = new Money(BigDecimal.TEN, USD);
        OrderLine line = new OrderLine(1L, 10L, 2, price);
        Order savedOrder = new Order(1L, 1L, List.of(line), USD);
        when(orderRepository.save(any())).thenReturn(savedOrder);

        Order result = useCase.execute(1L, 1L, List.of(line), USD);

        assertThat(result).isEqualTo(savedOrder);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getCustomerId()).isEqualTo(1L);
        verify(eventPublisher, times(1)).publish(any());
    }

    @Test
    void shouldThrowWhenDomainValidationFails() {
        assertThatThrownBy(() -> useCase.execute(null, 1L, List.of(), USD))
                .isInstanceOf(DomainException.class);
        verifyNoInteractions(orderRepository, eventPublisher);
    }
}
