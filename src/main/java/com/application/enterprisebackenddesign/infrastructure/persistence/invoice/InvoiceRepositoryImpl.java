package com.application.enterprisebackenddesign.infrastructure.persistence.invoice;

import com.application.enterprisebackenddesign.domain.invoice.Invoice;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceRepository;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceStatus;
import com.application.enterprisebackenddesign.infrastructure.persistence.invoice.entity.InvoiceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Adapter implementation of the InvoiceRepository port.
 *
 * Hexagonal Architecture: Follows the same adapter pattern as
 * CustomerRepositoryImpl and OrderRepositoryImpl. Translates between
 * domain Invoice objects and JPA InvoiceEntity objects via InvoiceMapper.
 *
 * The domain layer depends only on the InvoiceRepository interface;
 * this implementation can be swapped out (e.g., for MongoDB) without
 * changing any domain code.
 */
@Repository
public class InvoiceRepositoryImpl implements InvoiceRepository {

    private final SpringDataInvoiceRepository repository;
    private final InvoiceMapper invoiceMapper;

    public InvoiceRepositoryImpl(SpringDataInvoiceRepository repository, InvoiceMapper invoiceMapper) {
        this.repository = repository;
        this.invoiceMapper = invoiceMapper;
    }
    
    @Override
    public Invoice save(Invoice invoice) {
        InvoiceEntity entity = invoiceMapper.toEntity(invoice);
        InvoiceEntity saved = repository.save(entity);
        return invoiceMapper.toDomain(saved);
    }

    @Override
    public Optional<Invoice> findById(Long id) {
        return repository.findById(id).map(invoiceMapper::toDomain);
    }

    @Override
    public List<Invoice> findByOrderId(Long orderId) {
        return repository.findByOrderId(orderId).stream().map(invoiceMapper::toDomain).toList();
    }

    @Override
    public List<Invoice> findByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId).stream().map(invoiceMapper::toDomain).toList();
    }

    @Override
    public Page<Invoice> findByCustomerId(Long customerId, Pageable pageable) {
        return repository.findByCustomerId(customerId, pageable).map(invoiceMapper::toDomain);
    }

    @Override
    public List<Invoice> findByStatus(InvoiceStatus status) {
        return repository.findByStatus(status).stream().map(invoiceMapper::toDomain).toList();
    }

    @Override
    public Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable) {
        return repository.findByStatus(status, pageable).map(invoiceMapper::toDomain);
    }

    @Override
    public List<Invoice> findByCustomerIdAndStatus(Long customerId, InvoiceStatus status) {
        return repository.findByCustomerIdAndStatus(customerId, status)
                .stream()
                .map(invoiceMapper::toDomain)
                .toList();
    }

    @Override
    public Page<Invoice> findByCustomerIdAndStatus(Long customerId, InvoiceStatus status, Pageable pageable) {
        return repository.findByCustomerIdAndStatus(customerId, status, pageable).map(invoiceMapper::toDomain);
    }

    @Override
    public List<Invoice> findAll() {
        return repository.findAll().stream()
                .map(invoiceMapper::toDomain)
                .toList();
    }

    @Override
    public Page<Invoice> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(invoiceMapper::toDomain);
    }

}
