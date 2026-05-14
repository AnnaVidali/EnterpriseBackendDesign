package com.application.enterprisebackenddesign.api.invoice;

import com.application.enterprisebackenddesign.application.invoice.IssueInvoiceUseCase;
import com.application.enterprisebackenddesign.application.invoice.ListInvoicesUseCase;
import com.application.enterprisebackenddesign.domain.invoice.Invoice;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceRepository;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceStatus;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final IssueInvoiceUseCase issueInvoiceUseCase;
    private final ListInvoicesUseCase listInvoicesUseCase;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceRepository invoiceRepository;

    public InvoiceController(IssueInvoiceUseCase issueInvoiceUseCase, ListInvoicesUseCase listInvoicesUseCase,
                             InvoiceMapper invoiceMapper,
                             InvoiceRepository invoiceRepository) {
        this.issueInvoiceUseCase = issueInvoiceUseCase;
        this.listInvoicesUseCase = listInvoicesUseCase;
        this.invoiceMapper = invoiceMapper;
        this.invoiceRepository = invoiceRepository;
    }

    @GetMapping("/{invoiceId}")
    public InvoiceResponse getInvoice(@PathVariable Long invoiceId) throws DomainException.ResourceNotFoundException {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new DomainException.ResourceNotFoundException("Invoice not found with id: " + invoiceId));
        return invoiceMapper.toResponse(invoice);
    }

    @PostMapping("/issue/{orderId}")
    public InvoiceResponse issueInvoice(@PathVariable Long orderId) throws DomainException {
        Invoice invoice = issueInvoiceUseCase.execute(orderId);
        return invoiceMapper.toResponse(invoice);
    }

    @GetMapping
    public List<InvoiceResponse> getInvoices(@RequestParam(required = false) Long customerId, @RequestParam(required = false) InvoiceStatus status) {
        List<Invoice> invoices;
        if (customerId != null && status != null) {
            invoices = listInvoicesUseCase.listByCustomerIdAndStatus(customerId, status);
        } else if (customerId != null) {
            invoices = listInvoicesUseCase.listByCustomerId(customerId);
        } else if (status != null) {
            invoices = listInvoicesUseCase.listByStatus(status);
        } else {
            invoices = listInvoicesUseCase.listAll();
        }
        return invoices.stream()
                .map(invoiceMapper::toResponse)
                .collect(Collectors.toList());
    }
}
