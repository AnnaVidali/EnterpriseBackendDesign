package com.application.enterprisebackenddesign.infrastructure.persistence.invoice;

import com.application.enterprisebackenddesign.domain.invoice.InvoiceStatus;
import com.application.enterprisebackenddesign.infrastructure.persistence.invoice.entity.InvoiceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataInvoiceRepository extends JpaRepository<InvoiceEntity, Long> {

    List<InvoiceEntity> findByOrderId(Long orderId);

    List<InvoiceEntity> findByCustomerId(Long customerId);

    Page<InvoiceEntity> findByCustomerId(Long customerId, Pageable pageable);

    List<InvoiceEntity> findByStatus(InvoiceStatus status);

    Page<InvoiceEntity> findByStatus(InvoiceStatus status, Pageable pageable);

    List<InvoiceEntity> findByCustomerIdAndStatus(Long customerId, InvoiceStatus status);

    Page<InvoiceEntity> findByCustomerIdAndStatus(Long customerId, InvoiceStatus status, Pageable pageable);
}
