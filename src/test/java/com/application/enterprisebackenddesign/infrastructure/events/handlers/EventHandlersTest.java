package com.application.enterprisebackenddesign.infrastructure.events.handlers;

import com.application.enterprisebackenddesign.domain.invoice.InvoiceStatus;
import com.application.enterprisebackenddesign.domain.shared.*;
import com.application.enterprisebackenddesign.infrastructure.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventHandlersTest {

    @Mock private EmailService emailService;
    @Mock private CrmService crmService;
    @Mock private AnalyticsService analyticsService;
    @Mock private InventoryService inventoryService;

    @InjectMocks private InvoiceIssuedEventHandler invoiceIssuedHandler;
    @InjectMocks private PaymentCompletedEventHandler paymentCompletedHandler;
    @InjectMocks private PaymentFailedEventHandler paymentFailedHandler;
    @InjectMocks private OrderCreatedEventHandler orderCreatedHandler;
    @InjectMocks private OrderUpdatedEventHandler orderUpdatedHandler;
    @InjectMocks private CustomerCreatedEventHandler customerCreatedHandler;
    @InjectMocks private CustomerUpdatedEventHandler customerUpdatedHandler;
    @InjectMocks private ProductCreatedEventHandler productCreatedHandler;
    @InjectMocks private ProductUpdatedEventHandler productUpdatedHandler;
    @InjectMocks private InvoiceCreatedEventHandler invoiceCreatedHandler;

    private final Currency usd = Currency.getInstance("USD");

    @Test
    void invoiceIssuedHandlerSendsEmail() throws Exception {
        var event = new InvoiceIssuedEvent(1L, 1L, 1L, new Money(new BigDecimal("100"), usd));
        invoiceIssuedHandler.handle(event);
        verify(emailService).sendEmail("customer@example.com", "Invoice Issued",
                "Your invoice 1 for amount 100.00 USD has been issued.");
    }

    @Test
    void paymentCompletedHandlerSendsEmailAndUpdatesCrm() throws Exception {
        var event = new PaymentCompletedEvent(1L, 1L, 1L, 1L, new Money(new BigDecimal("100"), usd));
        paymentCompletedHandler.handle(event);
        verify(emailService).sendEmail("customer@example.com", "Payment Confirmed",
                "Your payment of 100.00 USD has been confirmed.");
        verify(crmService).updateOrderStatus(1L, "PAID");
    }

    @Test
    void paymentFailedHandlerSendsCustomerAndAdminEmail() {
        var event = new PaymentFailedEvent(1L, 1L, "Insufficient funds");
        paymentFailedHandler.handle(event);
        verify(emailService).sendEmail("customer@example.com", "Payment Failed",
                "Your payment for invoice 1 has failed. Reason: Insufficient funds");
        verify(emailService).sendEmail("admin@example.com", "Payment Alert",
                "Payment 1 failed. Reason: Insufficient funds");
    }

    @Test
    void orderCreatedHandlerTracksAnalyticsAndNotifiesWarehouse() {
        var event = new OrderCreatedEvent(1L, 1L, 3);
        orderCreatedHandler.handle(event);
        verify(analyticsService).trackOrderCreated(1L, 1L, 3);
        verify(inventoryService).notifyWarehouse(1L);
    }

    @Test
    void orderUpdatedHandlerUpdatesCrm() {
        var event = new OrderLineUpdatedEvent(DomainEventType.ORDER_UPDATED, 1L, 1L, 1L, 2, 5);
        orderUpdatedHandler.handle(event);
        verify(crmService).updateOrderStatus(1L, "UPDATED");
    }

    @Test
    void customerCreatedHandlerSendsWelcomeEmailAndSyncsCrm() {
        var event = new CustomerCreatedEvent(1L, "John", "Doe", "john@example.com");
        customerCreatedHandler.handle(event);
        verify(emailService).sendEmail("john@example.com", "Welcome to Enterprise Platform",
                "Hi John, welcome to our platform!");
        verify(crmService).syncCustomer(1L, "John Doe", "john@example.com");
    }

    @Test
    void customerUpdatedHandlerSyncsCrm() {
        var event = new CustomerUpdatedEvent(1L, "email", "old@example.com", "new@example.com");
        customerUpdatedHandler.handle(event);
        verify(crmService).syncCustomer(1L, "email", "new@example.com");
    }

    @Test
    void productCreatedHandlerUpdatesInventory() {
        var event = new ProductCreatedEvent(1L, "Widget", null, "WID-001");
        productCreatedHandler.handle(event);
        verify(inventoryService).updateProduct("WID-001", "Widget");
        verify(inventoryService).notifyWarehouse(1L);
    }

    @Test
    void productUpdatedHandlerUpdatesInventory() {
        var event = new ProductUpdatedEvent(1L, "name", "Old", "New");
        productUpdatedHandler.handle(event);
        verify(inventoryService).updateProduct("name", "New");
    }

    @Test
    void invoiceCreatedHandlerNotifiesAccounting() throws Exception {
        var event = new InvoiceCreatedEvent(1L, 1L, 1L, new Money(new BigDecimal("100"), usd), InvoiceStatus.DRAFT);
        invoiceCreatedHandler.handle(event);
        verify(emailService).sendEmail("accounting@example.com", "Invoice Created",
                "Invoice 1 for order 1 has been created.");
    }
}
