package com.application.enterprisebackenddesign.infrastructure.persistence.order;

import com.application.enterprisebackenddesign.TestcontainersConfiguration;
import com.application.enterprisebackenddesign.domain.customer.Customer;
import com.application.enterprisebackenddesign.domain.order.Order;
import com.application.enterprisebackenddesign.domain.order.OrderLine;
import com.application.enterprisebackenddesign.domain.order.OrderStatus;
import com.application.enterprisebackenddesign.domain.product.Product;
import com.application.enterprisebackenddesign.domain.shared.Money;
import com.application.enterprisebackenddesign.infrastructure.persistence.customer.CustomerRepositoryImpl;
import com.application.enterprisebackenddesign.infrastructure.persistence.product.ProductRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Sql(statements = {
        "DELETE FROM order_lines",
        "DELETE FROM orders",
        "DELETE FROM customers",
        "DELETE FROM products"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class OrderRepositoryImplIntegrationTest {

    @Autowired
    private OrderRepositoryImpl orderRepository;

    @Autowired
    private CustomerRepositoryImpl customerRepository;

    @Autowired
    private ProductRepositoryImpl productRepository;

    private Currency usd = Currency.getInstance("USD");

    @BeforeEach
    void setUp() throws Exception {
        customerRepository.save(new Customer(1L, "John", "Doe", "john@example.com"));
        productRepository.save(new Product(1L, "Widget", new Money(new BigDecimal("10.00"), usd), "WID-001"));
        productRepository.save(new Product(2L, "Gadget", new Money(new BigDecimal("20.00"), usd), "GDG-001"));
    }

    private OrderLine line(Long id, Long productId, int qty, double price) throws Exception {
        return new OrderLine(id, productId, qty, new Money(BigDecimal.valueOf(price), usd));
    }

    @Test
    void shouldSaveAndFindById() throws Exception {
        List<OrderLine> lines = List.of(line(1L, 1L, 2, 10.00));
        Order order = new Order(1L, 1L, lines, usd);
        Order saved = orderRepository.save(order);

        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getCustomerId()).isEqualTo(1L);
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(saved.getOrderLines()).hasSize(1);

        Optional<Order> found = orderRepository.findById(1L);
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(found.get().getOrderLines()).hasSize(1);
    }

    @Test
    void shouldSaveAndRetrieveOrderLines() throws Exception {
        List<OrderLine> lines = List.of(line(1L, 1L, 2, 10.00), line(2L, 2L, 1, 20.00));
        Order order = new Order(1L, 1L, lines, usd);
        orderRepository.save(order);

        Optional<Order> found = orderRepository.findById(1L);
        assertThat(found).isPresent();
        assertThat(found.get().getOrderLines()).hasSize(2);
        assertThat(found.get().getOrderLines().get(0).getProductId()).isEqualTo(1L);
        assertThat(found.get().getOrderLines().get(0).getQuantity()).isEqualTo(2);
        assertThat(found.get().getOrderLines().get(1).getProductId()).isEqualTo(2L);
        assertThat(found.get().getOrderLines().get(1).getQuantity()).isEqualTo(1);
    }

    @Test
    void shouldFindAll() throws Exception {
        orderRepository.save(new Order(1L, 1L, List.of(line(1L, 1L, 1, 10.00)), usd));
        orderRepository.save(new Order(2L, 1L, List.of(line(2L, 2L, 1, 20.00)), usd));

        List<Order> orders = orderRepository.findAll();
        assertThat(orders).hasSize(2);
    }

    @Test
    void shouldFindAllWithPagination() throws Exception {
        orderRepository.save(new Order(1L, 1L, List.of(line(1L, 1L, 1, 10.00)), usd));
        orderRepository.save(new Order(2L, 1L, List.of(line(2L, 2L, 1, 20.00)), usd));
        orderRepository.save(new Order(3L, 1L, List.of(line(3L, 1L, 1, 10.00)), usd));

        Page<Order> page = orderRepository.findAll(PageRequest.of(0, 2));
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    void shouldFindByStatus() throws Exception {
        orderRepository.save(new Order(1L, 1L, List.of(line(1L, 1L, 1, 10.00)), usd));

        List<Order> created = orderRepository.findByStatus(OrderStatus.CREATED);
        assertThat(created).hasSize(1);

        List<Order> confirmed = orderRepository.findByStatus(OrderStatus.CONFIRMED);
        assertThat(confirmed).isEmpty();
    }

    @Test
    void shouldFindByCustomerId() throws Exception {
        orderRepository.save(new Order(1L, 1L, List.of(line(1L, 1L, 1, 10.00)), usd));

        List<Order> orders = orderRepository.findByCustomerId(1L);
        assertThat(orders).hasSize(1);

        List<Order> noOrders = orderRepository.findByCustomerId(999L);
        assertThat(noOrders).isEmpty();
    }

    @Test
    void shouldFindByCustomerIdAndStatus() throws Exception {
        orderRepository.save(new Order(1L, 1L, List.of(line(1L, 1L, 1, 10.00)), usd));

        List<Order> result = orderRepository.findByCustomerIdAndStatus(1L, OrderStatus.CREATED);
        assertThat(result).hasSize(1);

        List<Order> noMatch = orderRepository.findByCustomerIdAndStatus(1L, OrderStatus.CONFIRMED);
        assertThat(noMatch).isEmpty();
    }

    @Test
    void shouldUpdateOrderStatus() throws Exception {
        orderRepository.save(new Order(1L, 1L, List.of(line(1L, 1L, 1, 10.00)), usd));

        Order updated = new Order(1L, 1L, List.of(line(1L, 1L, 1, 10.00)), usd, OrderStatus.CONFIRMED);
        orderRepository.save(updated);

        Optional<Order> found = orderRepository.findById(1L);
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        Optional<Order> found = orderRepository.findById(9999L);
        assertThat(found).isEmpty();
    }
}
