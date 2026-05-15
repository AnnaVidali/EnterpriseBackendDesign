package com.application.enterprisebackenddesign.application.payment;

import com.application.enterprisebackenddesign.application.shared.DomainEventPublisher;
import com.application.enterprisebackenddesign.application.shared.IdGenerator;
import com.application.enterprisebackenddesign.domain.invoice.Invoice;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceRepository;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceStatus;
import com.application.enterprisebackenddesign.domain.payment.Payment;
import com.application.enterprisebackenddesign.domain.payment.PaymentRepository;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import com.application.enterprisebackenddesign.infrastructure.external.GatewayPaymentRequest;
import com.application.enterprisebackenddesign.infrastructure.external.PaymentGateway;
import com.application.enterprisebackenddesign.infrastructure.external.PaymentResult;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

/**
 * Use case for processing a payment against an issued invoice.
 * Validates the payment matches the invoice amount, delegates
 * to an external payment gateway, and updates both payment and
 * invoice aggregates accordingly.
 *
 * Interview context: This is the most complex use case in the system.
 * It orchestrates multiple aggregates (Invoice + Payment), an external
 * service (PaymentGateway), and domain events — all within a single
 * @Transactional boundary. Key design points:
 *
 * 1. The @Transactional on the CLASS means ALL public methods run in
 *    a transaction. If the gateway call succeeds but save fails, the
 *    entire operation rolls back — the customer isn't charged without
 *    a recorded payment.
 *
 * 2. Event publication happens AFTER persistence (line 58: pullEvents
 *    is called after save). This prevents the "event published but
 *    transaction rolled back" race condition. If the save throws an
 *    exception, the events are never published because the method
 *    exits via exception before reaching the publish call.
 *
 * 3. The PaymentGateway interface isolates us from Stripe SDK changes.
 *    In tests, we mock it to simulate gateway failures and timeouts.
 *
 * 4. matchesInvoice() is called BEFORE the gateway — fail fast on
 *    amount mismatch saves an expensive HTTP call to Stripe.
 *
 * 5. ID generation uses IdGenerator (SecureRandom) instead of DB
 *    sequences, keeping the domain model persistence-agnostic.
 */
@Service
@Transactional
public class ProcessPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final DomainEventPublisher eventPublisher;
    private final PaymentGateway paymentGateway;
    private final IdGenerator idGenerator;

    public ProcessPaymentUseCase(PaymentRepository paymentRepository, InvoiceRepository invoiceRepository, DomainEventPublisher eventPublisher, PaymentGateway paymentGateway, IdGenerator idGenerator) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.eventPublisher = eventPublisher;
        this.paymentGateway = paymentGateway;
        this.idGenerator = idGenerator;
    }

    public Payment execute(Long invoiceID, Money paymentAmount) throws DomainException {
        Invoice invoice = invoiceRepository.findById(invoiceID).orElseThrow(() -> new DomainException.ResourceNotFoundException("Invoice not found"));
        if (!InvoiceStatus.ISSUED.equals(invoice.getStatus())) {
            throw new DomainException.BusinessRuleViolationException("Invoice status is not ISSUED");
        }
        Payment payment = new Payment(idGenerator.generateId(), invoice.getId(), invoice.getOrderId(), invoice.getCustomerId(), paymentAmount, com.application.enterprisebackenddesign.domain.payment.PaymentStatus.PENDING);
        if (!payment.matchesInvoice(invoice.getAmount())) {
            throw new DomainException.BusinessRuleViolationException("Payment amount does not match invoice amount.");
        }

        PaymentResult result = paymentGateway.process(new GatewayPaymentRequest(invoiceID, paymentAmount));
        if (result.success()) {
            payment.complete();
            invoice.markAsPaid();
        } else {
            payment.fail(result.message());
        }

        paymentRepository.save(payment);
        invoiceRepository.save(invoice);

        payment.pullEvents(true).forEach(eventPublisher::publish);
        invoice.pullEvents(true).forEach(eventPublisher::publish);

        return payment;
    }

}
