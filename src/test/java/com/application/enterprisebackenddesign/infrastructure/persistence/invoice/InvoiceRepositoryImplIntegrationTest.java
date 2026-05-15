package com.application.enterprisebackenddesign.infrastructure.persistence.invoice;

import com.application.enterprisebackenddesign.TestcontainersConfiguration;
import com.application.enterprisebackenddesign.domain.customer.Customer;
import com.application.enterprisebackenddesign.domain.invoice.Invoice;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceStatus;
import com.application.enterprisebackenddesign.domain.order.Order;
import com.application.enterprisebackenddesign.domain.order.OrderLine;
import com.application.enterprisebackenddesign.domain.product.Product;
import com.application.enterprisebackenddesign.domain.shared.Money;
import com.application.enterprisebackenddesign.infrastructure.persistence.customer.CustomerRepositoryImpl;
import com.application.enterprisebackenddesign.infrastructure.persistence.order.OrderRepositoryImpl;
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
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Sql(statements = {
        "DELETE FROM invoices",
        "DELETE FROM order_lines",
        "DELETE FROM orders",
        "DELETE FROM customers",
        "DELETE FROM products"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class InvoiceRepositoryImplIntegrationTest {

    @Autowired
    private InvoiceRepositoryImpl invoiceRepository;

    @Autowired
    private CustomerRepositoryImpl customerRepository;

    @Autowired
    private OrderRepositoryImpl orderRepository;

    @Autowired
    private ProductRepositoryImpl productRepository;

    private Currency usd = Currency.getInstance("USD");
    private Long customerId = 1L;
    private Long orderId = 1L;

    @BeforeEach
    void setUp() throws Exception {
        customerRepository.save(new Customer(customerId, "John", "Doe", "john@example.com"));
        productRepository.save(new Product(1L, "Widget", new Money(new BigDecimal("10.00"), usd), "WID-001"));
        Order order = new Order(orderId, customerId, List.of(new OrderLine(1L, 1L, 2, new Money(new BigDecimal("10.00"), usd))), usd);
        orderRepository.save(order);
    }

    private Money amount(double value) throws Exception {
        return new Money(BigDecimal.valueOf(value), usd);
    }

    @Test
    void shouldSaveAndFindById() throws Exception {
        Invoice invoice = new Invoice(1L, customerId, orderId, amount(20), InvoiceStatus.DRAFT, null);
        Invoice saved = invoiceRepository.save(invoice);

        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getCustomerId()).isEqualTo(customerId);
        assertThat(saved.getOrderId()).isEqualTo(orderId);
        assertThat(saved.getStatus()).isEqualTo(InvoiceStatus.DRAFT);

        Optional<Invoice> found = invoiceRepository.findById(1L);
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(InvoiceStatus.DRAFT);
    }

    @Test
    void shouldFindByOrderId() throws Exception {
        invoiceRepository.save(new Invoice(1L, customerId, orderId, amount(20), InvoiceStatus.DRAFT, null));

        List<Invoice> invoices = invoiceRepository.findByOrderId(orderId);
        assertThat(invoices).hasSize(1);

        List<Invoice> noMatch = invoiceRepository.findByOrderId(999L);
        assertThat(noMatch).isEmpty();
    }

    @Test
    void shouldFindByCustomerId() throws Exception {
        invoiceRepository.save(new Invoice(1L, customerId, orderId, amount(20), InvoiceStatus.DRAFT, null));

        List<Invoice> invoices = invoiceRepository.findByCustomerId(customerId);
        assertThat(invoices).hasSize(1);

        Page<Invoice> page = invoiceRepository.findByCustomerId(customerId, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void shouldFindByStatus() throws Exception {
        invoiceRepository.save(new Invoice(1L, customerId, orderId, amount(20), InvoiceStatus.DRAFT, null));

        List<Invoice> drafts = invoiceRepository.findByStatus(InvoiceStatus.DRAFT);
        assertThat(drafts).hasSize(1);

        List<Invoice> issued = invoiceRepository.findByStatus(InvoiceStatus.ISSUED);
        assertThat(issued).isEmpty();
    }

    @Test
    void shouldFindByCustomerIdAndStatus() throws Exception {
        invoiceRepository.save(new Invoice(1L, customerId, orderId, amount(20), InvoiceStatus.DRAFT, null));

        List<Invoice> result = invoiceRepository.findByCustomerIdAndStatus(customerId, InvoiceStatus.DRAFT);
        assertThat(result).hasSize(1);

        List<Invoice> noMatch = invoiceRepository.findByCustomerIdAndStatus(customerId, InvoiceStatus.PAID);
        assertThat(noMatch).isEmpty();
    }

    @Test
    void shouldFindAll() throws Exception {
        invoiceRepository.save(new Invoice(1L, customerId, orderId, amount(20), InvoiceStatus.DRAFT, null));
        invoiceRepository.save(new Invoice(2L, customerId, orderId, amount(30), InvoiceStatus.DRAFT, null));

        List<Invoice> invoices = invoiceRepository.findAll();
        assertThat(invoices).hasSize(2);

        Page<Invoice> page = invoiceRepository.findAll(PageRequest.of(0, 1));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldSaveIssuedInvoiceWithDate() throws Exception {
        Invoice invoice = new Invoice(1L, customerId, orderId, amount(20), InvoiceStatus.ISSUED, LocalDateTime.now());
        Invoice saved = invoiceRepository.save(invoice);

        assertThat(saved.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
        assertThat(saved.getInvoiceDate()).isNotNull();
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        Optional<Invoice> found = invoiceRepository.findById(9999L);
        assertThat(found).isEmpty();
    }
}
