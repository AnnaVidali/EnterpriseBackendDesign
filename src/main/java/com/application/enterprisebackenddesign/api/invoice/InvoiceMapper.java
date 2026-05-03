package com.application.enterprisebackenddesign.api.invoice;

import com.application.enterprisebackenddesign.domain.invoice.Invoice;
import org.springframework.stereotype.Component;

@Component
public class InvoiceMapper {

    public InvoiceResponse toResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getOrderId(),
                invoice.getCustomerId(),
                invoice.getAmount().getAmount(),
                invoice.getAmount().getCurrency().getCurrencyCode(),
                invoice.getStatus().name(),
                invoice.getInvoiceDate()
        );
    }
}
