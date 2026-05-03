package com.application.enterprisebackenddesign.api.invoice;

import com.application.enterprisebackenddesign.application.invoice.IssueInvoiceUseCase;
import com.application.enterprisebackenddesign.domain.invoice.Invoice;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceRepository;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final IssueInvoiceUseCase issueInvoiceUseCase;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceRepository invoiceRepository;

    public InvoiceController(IssueInvoiceUseCase issueInvoiceUseCase,
                             InvoiceMapper invoiceMapper,
                             InvoiceRepository invoiceRepository) {
        this.issueInvoiceUseCase = issueInvoiceUseCase;
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
}
