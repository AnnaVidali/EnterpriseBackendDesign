package com.application.enterprisebackenddesign.infrastructure.persistence.payment;

import com.application.enterprisebackenddesign.infrastructure.persistence.payment.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataPaymentRepository extends JpaRepository<PaymentEntity, Long> {

    List<PaymentEntity> findByInvoiceId(Long invoiceId);

    List<PaymentEntity> findByCustomerId(Long customerId);

    List<PaymentEntity> findByOrderId(Long orderId);

    List<PaymentEntity> findByStatus(String status);
}
