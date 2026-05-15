package com.application.enterprisebackenddesign.application.invoice;

import com.application.enterprisebackenddesign.domain.invoice.Invoice;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceRepository;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ListInvoicesUseCase {

    private final InvoiceRepository invoiceRepository;

    public ListInvoicesUseCase(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public List<Invoice> listAll() {
        return invoiceRepository.findAll();
    }

    public Page<Invoice> listAll(Pageable pageable) {
        return invoiceRepository.findAll(pageable);
    }

    public List<Invoice> listByCustomerId(Long id) {
        return invoiceRepository.findByCustomerId(id);
    }

    public Page<Invoice> listByCustomerId(Long id, Pageable pageable) {
        return invoiceRepository.findByCustomerId(id, pageable);
    }

    public List<Invoice> listByStatus(InvoiceStatus status) {
        return invoiceRepository.findByStatus(status);
    }

    public Page<Invoice> listByStatus(InvoiceStatus status, Pageable pageable) {
        return invoiceRepository.findByStatus(status, pageable);
    }

    public List<Invoice> listByCustomerIdAndStatus(Long customerId, InvoiceStatus status) {
        return invoiceRepository.findByCustomerIdAndStatus(customerId, status);
    }

    public Page<Invoice> listByCustomerIdAndStatus(Long customerId, InvoiceStatus status, Pageable pageable) {
        return invoiceRepository.findByCustomerIdAndStatus(customerId, status, pageable);
    }
}
