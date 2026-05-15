package com.application.enterprisebackenddesign.infrastructure.persistence.payment;

import com.application.enterprisebackenddesign.TestcontainersConfiguration;
import com.application.enterprisebackenddesign.domain.customer.Customer;
import com.application.enterprisebackenddesign.domain.invoice.Invoice;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceStatus;
import com.application.enterprisebackenddesign.domain.order.Order;
import com.application.enterprisebackenddesign.domain.order.OrderLine;
import com.application.enterprisebackenddesign.domain.payment.Payment;
import com.application.enterprisebackenddesign.domain.payment.PaymentStatus;
import com.application.enterprisebackenddesign.domain.product.Product;
import com.application.enterprisebackenddesign.domain.shared.Money;
import com.application.enterprisebackenddesign.infrastructure.persistence.customer.CustomerRepositoryImpl;
import com.application.enterprisebackenddesign.infrastructure.persistence.invoice.InvoiceRepositoryImpl;
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
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Sql(statements = {
        "DELETE FROM payments",
        "DELETE FROM invoices",
        "DELETE FROM order_lines",
        "DELETE FROM orders",
        "DELETE FROM customers",
        "DELETE FROM products"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PaymentRepositoryImplIntegrationTest {

    @Autowired
    private PaymentRepositoryImpl paymentRepository;

    @Autowired
    private CustomerRepositoryImpl customerRepository;

    @Autowired
    private OrderRepositoryImpl orderRepository;

    @Autowired
    private ProductRepositoryImpl productRepository;

    @Autowired
    private InvoiceRepositoryImpl invoiceRepository;

    private Currency usd = Currency.getInstance("USD");
    private Long customerId = 1L;
    private Long orderId = 1L;
    private Long invoiceId = 1L;

    @BeforeEach
    void setUp() throws Exception {
        customerRepository.save(new Customer(customerId, "John", "Doe", "john@example.com"));
        productRepository.save(new Product(1L, "Widget", new Money(new BigDecimal("20.00"), usd), "WID-001"));
        Order order = new Order(orderId, customerId, List.of(new OrderLine(1L, 1L, 1, new Money(new BigDecimal("20.00"), usd))), usd);
        orderRepository.save(order);
        Invoice invoice = new Invoice(invoiceId, customerId, orderId, new Money(new BigDecimal("20.00"), usd), InvoiceStatus.ISSUED, java.time.LocalDateTime.now());
        invoiceRepository.save(invoice);
    }

    private Money amount(double value) throws Exception {
        return new Money(BigDecimal.valueOf(value), usd);
    }

    @Test
    void shouldSaveAndFindById() throws Exception {
        Payment payment = new Payment(1L, invoiceId, orderId, customerId, amount(20), PaymentStatus.PENDING);
        Payment saved = paymentRepository.save(payment);

        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getInvoiceId()).isEqualTo(invoiceId);
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.PENDING);

        Optional<Payment> found = paymentRepository.findById(1L);
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void shouldFindByInvoiceId() throws Exception {
        paymentRepository.save(new Payment(1L, invoiceId, orderId, customerId, amount(20), PaymentStatus.PENDING));

        List<Payment> payments = paymentRepository.findByInvoiceId(invoiceId);
        assertThat(payments).hasSize(1);

        Page<Payment> page = paymentRepository.findByInvoiceId(invoiceId, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void shouldFindByCustomerId() throws Exception {
        paymentRepository.save(new Payment(1L, invoiceId, orderId, customerId, amount(20), PaymentStatus.PENDING));

        List<Payment> payments = paymentRepository.findByCustomerId(customerId);
        assertThat(payments).hasSize(1);

        Page<Payment> page = paymentRepository.findByCustomerId(customerId, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void shouldFindByStatus() throws Exception {
        paymentRepository.save(new Payment(1L, invoiceId, orderId, customerId, amount(20), PaymentStatus.PENDING));
        paymentRepository.save(new Payment(2L, invoiceId, orderId, customerId, amount(20), PaymentStatus.COMPLETED));

        List<Payment> pending = paymentRepository.findByStatus(PaymentStatus.PENDING);
        assertThat(pending).hasSize(1);

        List<Payment> failed = paymentRepository.findByStatus(PaymentStatus.FAILED);
        assertThat(failed).isEmpty();
    }

    @Test
    void shouldFindByInvoiceIdAndCustomerIdAndStatus() throws Exception {
        paymentRepository.save(new Payment(1L, invoiceId, orderId, customerId, amount(20), PaymentStatus.PENDING));

        List<Payment> result = paymentRepository.findByInvoiceIdAndCustomerIdAndStatus(invoiceId, customerId, PaymentStatus.PENDING);
        assertThat(result).hasSize(1);

        List<Payment> noMatch = paymentRepository.findByInvoiceIdAndCustomerIdAndStatus(invoiceId, customerId, PaymentStatus.COMPLETED);
        assertThat(noMatch).isEmpty();
    }

    @Test
    void shouldFindAll() throws Exception {
        paymentRepository.save(new Payment(1L, invoiceId, orderId, customerId, amount(20), PaymentStatus.PENDING));
        paymentRepository.save(new Payment(2L, invoiceId, orderId, customerId, amount(30), PaymentStatus.PENDING));

        List<Payment> payments = paymentRepository.findAll();
        assertThat(payments).hasSize(2);

        Page<Payment> page = paymentRepository.findAll(PageRequest.of(0, 1));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        Optional<Payment> found = paymentRepository.findById(9999L);
        assertThat(found).isEmpty();
    }
}
