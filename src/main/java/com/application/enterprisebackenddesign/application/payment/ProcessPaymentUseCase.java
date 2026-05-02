package com.application.enterprisebackenddesign.application.payment;

import com.application.enterprisebackenddesign.application.shared.DomainEventPublisher;
import com.application.enterprisebackenddesign.domain.invoice.Invoice;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceRepository;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceStatus;
import com.application.enterprisebackenddesign.domain.payment.Payment;
import com.application.enterprisebackenddesign.domain.payment.PaymentRepository;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ProcessPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final DomainEventPublisher eventPublisher;

    public ProcessPaymentUseCase(PaymentRepository paymentRepository, InvoiceRepository invoiceRepository, DomainEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.eventPublisher = eventPublisher;
    }

    public Payment execute(Long invoiceID, Money paymentAmount) throws DomainException {
        Invoice invoice = invoiceRepository.findById(invoiceID).orElseThrow(() -> new DomainException.ResourceNotFoundException("Invoice not found"));
        if (!InvoiceStatus.ISSUED.equals(invoice.getStatus())) {
            throw new DomainException.BusinessRuleViolationException("Invoice status is not ISSUED");
        }
        Long paymentId = generatePaymentId();
        Payment payment = new Payment(paymentId, invoice.getId(), invoice.getOrderId(), invoice.getCustomerId(), paymentAmount, com.application.enterprisebackenddesign.domain.payment.PaymentStatus.PENDING);
        if (!payment.matchesInvoice(invoice.getAmount())) {
            throw new DomainException.BusinessRuleViolationException("Payment amount does not match invoice amount.");
        }
        payment.complete();
        invoice.markAsPaid();

        paymentRepository.save(payment);
        invoiceRepository.save(invoice);

        payment.pullEvents(true).forEach(eventPublisher::publish);
        invoice.pullEvents(true).forEach(eventPublisher::publish);

        return payment;
    }

    private Long generatePaymentId() {
        return System.currentTimeMillis();
    }
}
