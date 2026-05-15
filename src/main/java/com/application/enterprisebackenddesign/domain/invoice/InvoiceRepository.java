package com.application.enterprisebackenddesign.domain.invoice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository {

    Invoice save(Invoice invoice);

    Optional<Invoice> findById(Long id);

    List<Invoice> findByOrderId(Long orderId);

    List<Invoice> findByCustomerId(Long customerId);

    Page<Invoice> findByCustomerId(Long customerId, Pageable pageable);

    List<Invoice> findByStatus(InvoiceStatus status);

    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);

    List<Invoice> findByCustomerIdAndStatus(Long customerId, InvoiceStatus status);

    Page<Invoice> findByCustomerIdAndStatus(Long customerId, InvoiceStatus status, Pageable pageable);

    List<Invoice> findAll();

    Page<Invoice> findAll(Pageable pageable);

}
