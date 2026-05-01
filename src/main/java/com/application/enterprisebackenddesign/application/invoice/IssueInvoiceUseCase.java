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
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new DomainException.BusinessRuleViolationException("Order not found"));

        if(order.getStatus() != OrderStatus.CONFIRMED){
            throw new DomainException.BusinessRuleViolationException("Order status is not CONFIRMED");
        }

        List<Invoice> invoice = invoiceRepository.findByOrderId(orderId);

        Invoice invoiceToSave = null;

        for(Invoice invoiceItem : invoice){
            if(invoiceItem.getStatus() == InvoiceStatus.DRAFT) {
                invoiceToSave = invoiceItem;
                break;
            }
        }

        if(invoiceToSave == null) {
            // Using orderId as invoiceId since we enforce one invoice per order
            invoiceToSave = Invoice.fromOrder(orderId, orderId, order.getCustomerId(), order.getTotalAmount());
        }

        invoiceToSave.issue();

        Invoice savedInvoice = invoiceRepository.save(invoiceToSave);
        savedInvoice.getEvents().forEach(eventPublisher::publish);
        savedInvoice.clearEvents();

        return savedInvoice;
    }
}
