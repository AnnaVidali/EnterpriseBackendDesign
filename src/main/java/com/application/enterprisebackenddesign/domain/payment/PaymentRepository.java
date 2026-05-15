package com.application.enterprisebackenddesign.domain.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for the Payment aggregate.
 *
 * Hexagonal Architecture (Port): PaymentRepository defines how payments are
 * persisted and queried. The findByInvoiceIdAndCustomerIdAndStatus method
 * supports the specific query pattern needed by the payment workflow
 * (duplicate detection, status checks before re-processing).
 *
 * DDD design choice: The payment repository accepts domain Payment objects
 * and returns domain Payment objects. The mapping to/from persistence is
 * handled entirely by the adapter implementation (PaymentRepositoryImpl).
 */
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
