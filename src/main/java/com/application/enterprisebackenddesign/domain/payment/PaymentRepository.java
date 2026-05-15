package com.application.enterprisebackenddesign.domain.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    List<Payment> findAll();

    Page<Payment> findAll(Pageable pageable);

    List<Payment> findByInvoiceId(Long invoiceId);

    Page<Payment> findByInvoiceId(Long invoiceId, Pageable pageable);

    List<Payment> findByCustomerId(Long customerId);

    Page<Payment> findByCustomerId(Long customerId, Pageable pageable);

    List<Payment> findByStatus(PaymentStatus status);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    List<Payment> findByInvoiceIdAndCustomerIdAndStatus(Long invoiceId, Long customerId, PaymentStatus status);

    Page<Payment> findByInvoiceIdAndCustomerIdAndStatus(Long invoiceId, Long customerId, PaymentStatus status, Pageable pageable);
}
