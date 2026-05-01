package com.application.enterprisebackenddesign.application.invoice;

import com.application.enterprisebackenddesign.application.shared.DomainEventPublisher;
import com.application.enterprisebackenddesign.domain.invoice.Invoice;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceRepository;
import com.application.enterprisebackenddesign.domain.shared.DomainEvent;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.OrderBilledEvent;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class PayInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final DomainEventPublisher eventPublisher;

    public PayInvoiceUseCase(InvoiceRepository invoiceRepository, DomainEventPublisher eventPublisher) {
        this.invoiceRepository = invoiceRepository;
        this.eventPublisher = eventPublisher;
    }

    public Invoice execute(Long invoiceId) throws DomainException {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new DomainException.BusinessRuleViolationException("Invoice not found"));

        invoice.markAsPaid();

        invoiceRepository.save(invoice);
        eventPublisher.publish(new OrderBilledEvent(invoice.getOrderId(), invoice.getCustomerId(), invoice.getAmount()));

        return invoice;
    }
}
