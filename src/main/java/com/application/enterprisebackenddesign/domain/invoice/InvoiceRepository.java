package com.application.enterprisebackenddesign.domain.invoice;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository {

    Invoice save(Invoice invoice);

    Optional<Invoice> findById(Long id);

    List<Invoice> findByOrderId(Long orderId);

    List<Invoice> findByCustomerId(Long customerId);

    List<Invoice> findByStatus(InvoiceStatus status);

    void deleteById(Long id);

    boolean existsById(Long id);
}
