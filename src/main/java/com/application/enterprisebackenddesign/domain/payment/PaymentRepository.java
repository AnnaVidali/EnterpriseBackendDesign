package com.application.enterprisebackenddesign.domain.payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    List<Payment> findAll();

    List<Payment> findByInvoiceId(Long invoiceId);

    List<Payment> findByCustomerId(Long customerId);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByInvoiceIdAndCustomerIdAndStatus(Long invoiceId, Long customerId, PaymentStatus status);
}
