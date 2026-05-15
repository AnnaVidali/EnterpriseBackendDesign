package com.application.enterprisebackenddesign.infrastructure.persistence.payment;

import com.application.enterprisebackenddesign.domain.payment.PaymentStatus;
import com.application.enterprisebackenddesign.infrastructure.persistence.payment.entity.PaymentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataPaymentRepository extends JpaRepository<PaymentEntity, Long> {

    List<PaymentEntity> findByInvoiceId(Long invoiceId);

    Page<PaymentEntity> findByInvoiceId(Long invoiceId, Pageable pageable);

    List<PaymentEntity> findByCustomerId(Long customerId);

    Page<PaymentEntity> findByCustomerId(Long customerId, Pageable pageable);

    List<PaymentEntity> findByStatus(PaymentStatus status);

    Page<PaymentEntity> findByStatus(PaymentStatus status, Pageable pageable);

    List<PaymentEntity> findByInvoiceIdAndCustomerIdAndStatus(Long invoiceId, Long customerId, PaymentStatus status);

    Page<PaymentEntity> findByInvoiceIdAndCustomerIdAndStatus(Long invoiceId, Long customerId, PaymentStatus status, Pageable pageable);
}
