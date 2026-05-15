package com.application.enterprisebackenddesign.domain.payment;

import com.application.enterprisebackenddesign.domain.shared.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate root representing a payment transaction against an invoice.
 * Manages lifecycle from PENDING to COMPLETED or FAILED and validates
 * that the payment amount matches the invoice before processing.
 *
 * Interview context: Payment is the final step in the order-to-cash flow.
 * The ProcessPaymentUseCase creates a PENDING payment, calls the external
 * gateway, then transitions to COMPLETED or FAILED based on the result.
 *
 * Important design decisions:
 * 1. matchesInvoice() is called BEFORE calling the payment gateway —
 *    failing fast on amount mismatch saves an expensive API call and
 *    provides immediate feedback to the client.
 * 2. The gateway integration is abstracted behind an interface (PaymentGateway),
 *    allowing the use case to be tested without real HTTP calls.
 * 3. Payment status transitions are strictly guarded: PENDING → COMPLETED
 *    or PENDING → FAILED. You cannot retry a COMPLETED payment or complete
 *    a FAILED one. This matches real payment gateway behavior.
 * 4. paymentDate is set to null on creation and populated on completion —
 *    null means "not yet completed", which is semantically clearer than
 *    a sentinel date value.
 * 5. ID generation uses a shared IdGenerator with SecureRandom (see
 *    ticket 28). This was changed from UUID.randomUUID().getMostSignificantBits()
 *    to avoid potential collisions under concurrent load and to make the
 *    ID generation strategy explicit and testable.
 */
@Getter
public class Payment {

    private final Long id;
    private final Long invoiceId;
    private final Long orderId;
    private final Long customerId;
    private final Money amount;
    private PaymentStatus status;
    private LocalDateTime paymentDate;
    private final List<DomainEvent> events = new ArrayList<>();

    public Payment(Long id, Long invoiceId, Long orderId, Long customerId, Money amount, PaymentStatus status) throws DomainException.BusinessRuleViolationException {
        if(id == null){
            throw new DomainException.BusinessRuleViolationException("Id cannot be null.");
        }
        this.id = id;
        if(invoiceId == null){
            throw new DomainException.BusinessRuleViolationException("Invoice id cannot be null.");
        }
        this.invoiceId = invoiceId;
        if(orderId == null){
            throw new DomainException.BusinessRuleViolationException("Order id cannot be null.");
        }
        this.orderId = orderId;
        if(customerId == null){
            throw new DomainException.BusinessRuleViolationException("Customer id cannot be null.");
        }
        this.customerId = customerId;
        if (amount == null || amount.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException.BusinessRuleViolationException("Amount must be greater than zero.");
        }
        this.amount = amount;
        if (status == null) {
            throw new DomainException.BusinessRuleViolationException("Status cannot be null.");
        }
        this.status = status;
        this.paymentDate = null;
        events.add(new PaymentPendingEvent(this.id, this.invoiceId, this.amount));
    }

    public void complete() throws DomainException.BusinessRuleViolationException {
        if (status == PaymentStatus.COMPLETED) {
            throw new DomainException.BusinessRuleViolationException("Payment is already COMPLETED.");
        }
        if (status != PaymentStatus.PENDING) {
            throw new DomainException.BusinessRuleViolationException("Status must be PENDING.");
        }
        status = PaymentStatus.COMPLETED;
        paymentDate = LocalDateTime.now();
        events.add(new PaymentCompletedEvent(id, invoiceId, orderId, customerId, amount));
    }

    public void fail(String reason) throws DomainException.BusinessRuleViolationException {
        if (status != PaymentStatus.PENDING) {
            throw new DomainException.BusinessRuleViolationException("Status must be PENDING.");
        }
        status = PaymentStatus.FAILED;
        events.add(new PaymentFailedEvent(id, invoiceId, reason));
    }

    public List<DomainEvent> pullEvents(boolean clear) {

        List<DomainEvent> copiedEvents = new ArrayList<>(events);
        if(clear) {
            clearEvents();
        }
        return copiedEvents;
    }

    public Boolean matchesInvoice(Money invoiceAmount) {
        return invoiceAmount.equals(amount) && invoiceAmount.getCurrency().equals(amount.getCurrency());
    }

    public void clearEvents() {
        events.clear();
    }
}
