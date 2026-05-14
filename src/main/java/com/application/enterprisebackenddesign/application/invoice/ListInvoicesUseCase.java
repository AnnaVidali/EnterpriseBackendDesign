package com.application.enterprisebackenddesign.application.invoice;

import com.application.enterprisebackenddesign.domain.invoice.Invoice;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceRepository;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceStatus;
import jakarta.transaction.Transactional;
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

    public List<Invoice> listByCustomerId(Long id) {
        return invoiceRepository.findByCustomerId(id);
    }

    public List<Invoice> listByStatus(InvoiceStatus status) {
        return invoiceRepository.findByStatus(status);
    }

    public List<Invoice> listByCustomerIdAndStatus(Long customerId, InvoiceStatus status) {
        return invoiceRepository.findByCustomerIdAndStatus(customerId, status);
    }
}
