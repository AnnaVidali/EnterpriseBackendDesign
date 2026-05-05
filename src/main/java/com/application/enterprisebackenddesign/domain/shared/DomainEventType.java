package com.application.enterprisebackenddesign.domain.shared;

public enum DomainEventType {
    ORDER_CREATED,
    ORDER_CONFIRMED,
    ORDER_CANCELLED,
    ORDER_BILLED,
    ORDER_UPDATED,
    INVOICE_ISSUED,
    PAYMENT_PENDING,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED,
    CUSTOMER_CREATED,
    CUSTOMER_UPDATED
}
