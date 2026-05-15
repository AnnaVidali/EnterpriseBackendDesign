package com.application.enterprisebackenddesign.application.invoice;

import com.application.enterprisebackenddesign.application.shared.DomainEventPublisher;
import com.application.enterprisebackenddesign.domain.invoice.Invoice;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceRepository;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceStatus;
import com.application.enterprisebackenddesign.domain.order.Order;
import com.application.enterprisebackenddesign.domain.order.OrderRepository;
import com.application.enterprisebackenddesign.domain.order.OrderStatus;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Use case for issuing an invoice against a confirmed order.
 * Creates or retrieves a DRAFT invoice, transitions it to ISSUED,
 * persists the change, and publishes domain events.
 */
@Service
@Transactional
public class IssueInvoiceUseCase {

    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final DomainEventPublisher eventPublisher;

    public IssueInvoiceUseCase(OrderRepository orderRepository, InvoiceRepository invoiceRepository, DomainEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.invoiceRepository = invoiceRepository;
        this.eventPublisher = eventPublisher;
    }

    public Invoice execute (Long orderId) throws DomainException {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new DomainException.ResourceNotFoundException("Order not found"));

        if(order.getStatus() != OrderStatus.CONFIRMED){
            throw new DomainException.BusinessRuleViolationException("Order status is not CONFIRMED");
        }

        Invoice invoiceToSave = invoiceRepository.findByOrderId(orderId).stream()
                .filter(i -> i.getStatus() == InvoiceStatus.DRAFT)
                .findFirst()
                .orElseGet(() -> {
                    try {
                        return Invoice.fromOrder(orderId, orderId, order.getCustomerId(), order.getTotalAmount());
                    } catch (DomainException e) {
                        throw new RuntimeException(e);
                    }
                });

        invoiceToSave.issue();

        Invoice savedInvoice = invoiceRepository.save(invoiceToSave);
        invoiceToSave.pullEvents(true).forEach(eventPublisher::publish);

        return savedInvoice;
    }
}
