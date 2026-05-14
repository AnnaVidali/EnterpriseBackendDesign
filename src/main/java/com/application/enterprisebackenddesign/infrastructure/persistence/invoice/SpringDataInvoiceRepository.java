package com.application.enterprisebackenddesign.infrastructure.persistence.invoice;

import com.application.enterprisebackenddesign.domain.invoice.InvoiceStatus;
import com.application.enterprisebackenddesign.infrastructure.persistence.invoice.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataInvoiceRepository extends JpaRepository<InvoiceEntity, Long> {

    List<InvoiceEntity> findByOrderId(Long orderId);

    List<InvoiceEntity> findByCustomerId(Long customerId);

    List<InvoiceEntity> findByStatus(InvoiceStatus status);

    List<InvoiceEntity> findByCustomerIdAndStatus(Long customerId, InvoiceStatus status);
}
