package com.application.enterprisebackenddesign.infrastructure.persistence.payment;

import com.application.enterprisebackenddesign.domain.invoice.Invoice;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceStatus;
import com.application.enterprisebackenddesign.domain.payment.Payment;
import com.application.enterprisebackenddesign.domain.payment.PaymentStatus;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import com.application.enterprisebackenddesign.infrastructure.persistence.payment.entity.PaymentEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class PaymentMapper {

    public PaymentEntity toEntity(Payment payment) {
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setId(payment.getId());
        paymentEntity.setInvoiceId(payment.getInvoiceId());
        paymentEntity.setOrderId(payment.getOrderId());
        paymentEntity.setCustomerId(payment.getCustomerId());
        paymentEntity.setStatus(PaymentStatus.valueOf(payment.getStatus().name()));
        paymentEntity.setPaymentDate(payment.getPaymentDate());
        paymentEntity.setAmount(payment.getAmount().getAmount());
        paymentEntity.setCurrency(payment.getAmount().getCurrency().getCurrencyCode());
        return paymentEntity;
    }

    public Payment toDomain(PaymentEntity paymentEntity) {
        try {
            return new Payment(paymentEntity.getId(), paymentEntity.getInvoiceId(), paymentEntity.getOrderId(), paymentEntity.getCustomerId(), new Money(paymentEntity.getAmount(), Currency.getInstance(paymentEntity.getCurrency())), paymentEntity.getStatus());
        } catch (DomainException.BusinessRuleViolationException e) {
            throw new IllegalStateException("Corrupted payment data in database", e);
        }
    }
}
