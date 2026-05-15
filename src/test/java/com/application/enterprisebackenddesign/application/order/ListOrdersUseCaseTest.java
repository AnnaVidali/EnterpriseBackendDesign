package com.application.enterprisebackenddesign.application.order;

import com.application.enterprisebackenddesign.domain.order.Order;
import com.application.enterprisebackenddesign.domain.order.OrderLine;
import com.application.enterprisebackenddesign.domain.order.OrderRepository;
import com.application.enterprisebackenddesign.domain.order.OrderStatus;
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
class ListOrdersUseCaseTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Mock
    private OrderRepository orderRepository;
    @InjectMocks
    private ListOrdersUseCase useCase;

    private Money price() throws DomainException {
        return new Money(BigDecimal.TEN, USD);
    }

    private OrderLine line() throws DomainException {
        return new OrderLine(1L, 10L, 2, price());
    }

    @Test
    void shouldReturnAllOrders() throws DomainException {
        List<Order> orders = List.of(
                new Order(1L, 1L, List.of(line()), USD),
                new Order(2L, 2L, List.of(line()), USD)
        );
        when(orderRepository.findAll()).thenReturn(orders);

        List<Order> result = useCase.listAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldReturnPagedOrders() throws DomainException {
        List<Order> orders = List.of(new Order(1L, 1L, List.of(line()), USD));
        Pageable pageable = PageRequest.of(0, 20);
        when(orderRepository.findAll(pageable)).thenReturn(new PageImpl<>(orders, pageable, 1));

        Page<Order> result = useCase.listAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldReturnOrdersByCustomerId() throws DomainException {
        List<Order> orders = List.of(new Order(1L, 1L, List.of(line()), USD));
        when(orderRepository.findByCustomerId(1L)).thenReturn(orders);

        List<Order> result = useCase.listByCustomerId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldReturnPagedOrdersByCustomerId() throws DomainException {
        List<Order> orders = List.of(new Order(1L, 1L, List.of(line()), USD));
        Pageable pageable = PageRequest.of(0, 20);
        when(orderRepository.findByCustomerId(1L, pageable)).thenReturn(new PageImpl<>(orders, pageable, 1));

        Page<Order> result = useCase.listByCustomerId(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldReturnOrdersByStatus() throws DomainException {
        List<Order> orders = List.of(new Order(1L, 1L, List.of(line()), USD));
        when(orderRepository.findByStatus(OrderStatus.CREATED)).thenReturn(orders);

        List<Order> result = useCase.listByStatus(OrderStatus.CREATED);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldReturnPagedOrdersByStatus() throws DomainException {
        List<Order> orders = List.of(new Order(1L, 1L, List.of(line()), USD));
        Pageable pageable = PageRequest.of(0, 20);
        when(orderRepository.findByStatus(OrderStatus.CREATED, pageable)).thenReturn(new PageImpl<>(orders, pageable, 1));

        Page<Order> result = useCase.listByStatus(OrderStatus.CREATED, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldReturnOrdersByCustomerIdAndStatus() throws DomainException {
        List<Order> orders = List.of(new Order(1L, 1L, List.of(line()), USD));
        when(orderRepository.findByCustomerIdAndStatus(1L, OrderStatus.CREATED)).thenReturn(orders);

        List<Order> result = useCase.listByCustomerIdAndStatus(1L, OrderStatus.CREATED);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldReturnPagedOrdersByCustomerIdAndStatus() throws DomainException {
        List<Order> orders = List.of(new Order(1L, 1L, List.of(line()), USD));
        Pageable pageable = PageRequest.of(0, 20);
        when(orderRepository.findByCustomerIdAndStatus(1L, OrderStatus.CREATED, pageable))
                .thenReturn(new PageImpl<>(orders, pageable, 1));

        Page<Order> result = useCase.listByCustomerIdAndStatus(1L, OrderStatus.CREATED, pageable);

        assertThat(result.getContent()).hasSize(1);
    }
}
