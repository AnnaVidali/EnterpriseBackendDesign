package com.application.enterprisebackenddesign.infrastructure.persistence.invoice;

import com.application.enterprisebackenddesign.domain.invoice.Invoice;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceStatus;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import com.application.enterprisebackenddesign.infrastructure.persistence.invoice.entity.InvoiceEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class InvoiceMapper {

    public InvoiceEntity toEntity(Invoice invoice) {
        InvoiceEntity invoiceEntity = new InvoiceEntity();
        invoiceEntity.setId(invoice.getId());
        invoiceEntity.setCustomerId(invoice.getCustomerId());
        invoiceEntity.setOrderId(invoice.getOrderId());
        invoiceEntity.setInvoiceDate(invoice.getInvoiceDate());
        invoiceEntity.setStatus(invoice.getStatus().name());
        invoiceEntity.setAmount(invoice.getAmount().getAmount());
        invoiceEntity.setCurrency(invoice.getAmount().getCurrency().getCurrencyCode());
        return invoiceEntity;
    }

    public Invoice toDomain(InvoiceEntity invoiceEntity) {
        try {
            return new Invoice(invoiceEntity.getId(), invoiceEntity.getCustomerId(), invoiceEntity.getOrderId(), new Money(invoiceEntity.getAmount(), Currency.getInstance(invoiceEntity.getCurrency())), InvoiceStatus.valueOf(invoiceEntity.getStatus()), invoiceEntity.getInvoiceDate());
        } catch (DomainException.BusinessRuleViolationException e) {
            throw new IllegalStateException("Corrupted invoice data in database", e);
        }
    }
}
