package com.application.enterprisebackenddesign.domain.invoice;

import com.application.enterprisebackenddesign.domain.shared.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Invoice {

    private final Long id;
    private final Long customerId;
    private final Long orderId;
    private final Money amount;
    private InvoiceStatus status;
    private LocalDateTime invoiceDate;
    private final List<DomainEvent> events = new ArrayList<>();

    public Invoice(Long id, Long customerId, Long orderId, Money amount, InvoiceStatus status, LocalDateTime invoiceDate) throws DomainException.BusinessRuleViolationException {
        if(id == null){
            throw new DomainException.BusinessRuleViolationException("Id cannot be null.");
        }
        this.id = id;
        if(customerId == null){
            throw new DomainException.BusinessRuleViolationException("Customer id cannot be null.");
        }
        this.customerId = customerId;
        if(orderId == null){
            throw new DomainException.BusinessRuleViolationException("Order id cannot be null.");
        }
        this.orderId = orderId;
        if (amount == null || amount.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException.BusinessRuleViolationException("Amount must be greater than zero.");
        }
        this.amount = amount;
        if (status == null) {
            throw new DomainException.BusinessRuleViolationException("Status cannot be null.");
        }
        this.status = status;
        if ((status == InvoiceStatus.ISSUED || status == InvoiceStatus.PAID) && invoiceDate == null) {
            throw new DomainException.BusinessRuleViolationException("Issued or paid invoices must have an invoice date.");
        }
        this.invoiceDate = invoiceDate;
        events.add(new InvoiceCreatedEvent(this.id, this.customerId, this.orderId, this.amount, this.status));
    }

    public void issue() throws DomainException.BusinessRuleViolationException {
        if (status != InvoiceStatus.DRAFT) {
            throw new DomainException.BusinessRuleViolationException("Only DRAFT invoices can be issued.");
        }
        status = InvoiceStatus.ISSUED;
        invoiceDate = LocalDateTime.now();
        events.add(new InvoiceIssuedEvent(id, orderId, customerId, amount));
    }

    public void markAsPaid() throws DomainException.BusinessRuleViolationException {
        if (status != InvoiceStatus.ISSUED) {
            throw new DomainException.BusinessRuleViolationException("Only ISSUED invoices can be marked as PAID.");
        }
        status = InvoiceStatus.PAID;
        events.add(new com.application.enterprisebackenddesign.domain.shared.OrderBilledEvent(orderId, customerId, amount));
    }

    public void cancel() throws DomainException.BusinessRuleViolationException {
        if(status == InvoiceStatus.PAID){
            throw new DomainException.BusinessRuleViolationException("Cannot cancel a PAID invoice.");
        }
        if (status == InvoiceStatus.CANCELLED) {
            return;
        }
        status = InvoiceStatus.CANCELLED;
    }

    public static Invoice fromOrder(Long id, Long orderId, Long customerId, Money amount) throws DomainException {
        return new Invoice(id, customerId, orderId, amount, InvoiceStatus.DRAFT, null);
    }

    public List<DomainEvent> getEvents() {
        return List.copyOf(events);
    }

    public void clearEvents() {
        events.clear();
    }

    public List<DomainEvent> pullEvents(boolean clear) {
        List<DomainEvent> copiedEvents = new ArrayList<>(events);
        if (clear) {
            clearEvents();
        }
        return copiedEvents;
    }

    @Override
    public String toString() {
        return "Invoice {\n" +
                "\tid = " + id +
                ",\n\tcustomerId = " + customerId +
                ",\n\torderId = " + orderId +
                ",\n\tamount = " + amount +
                ",\n\tstatus = " + status +
                ",\n\tinvoiceDate = " + invoiceDate +
                "\n}";
    }
}
