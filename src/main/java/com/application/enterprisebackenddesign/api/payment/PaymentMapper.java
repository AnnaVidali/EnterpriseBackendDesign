package com.application.enterprisebackenddesign.api.payment;

import com.application.enterprisebackenddesign.domain.payment.Payment;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getInvoiceId(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getAmount().getAmount(),
                payment.getAmount().getCurrency().getCurrencyCode(),
                payment.getStatus().name(),
                payment.getPaymentDate()
        );
    }

    public Money toMoney(PaymentRequest request) throws DomainException {
        return new Money(request.amount(), Currency.getInstance(request.currency()));
    }
}
