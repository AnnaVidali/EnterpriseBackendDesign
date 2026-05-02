package com.application.enterprisebackenddesign.domain.payment;

import com.application.enterprisebackenddesign.domain.shared.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
        this.orderId = orderId;
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
        events.add(new PaymentPendingEvent(id, invoiceId, amount));
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
