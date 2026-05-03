package com.application.enterprisebackenddesign.domain.payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    List<Payment> findByInvoiceId(Long invoiceId);
}
