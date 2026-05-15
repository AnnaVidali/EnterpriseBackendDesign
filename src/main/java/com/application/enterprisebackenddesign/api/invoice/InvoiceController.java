package com.application.enterprisebackenddesign.api.invoice;

import com.application.enterprisebackenddesign.api.shared.PageResponse;
import com.application.enterprisebackenddesign.application.invoice.IssueInvoiceUseCase;
import com.application.enterprisebackenddesign.application.invoice.ListInvoicesUseCase;
import com.application.enterprisebackenddesign.domain.invoice.Invoice;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceRepository;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceStatus;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
@Tag(name = "Invoices", description = "Invoice lifecycle — issue invoices from orders and retrieve invoice details")
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
    @Operation(summary = "Get an invoice by ID",
            description = "Returns invoice details including amount, status, and customer/order references.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invoice found",
                    content = @Content(schema = @Schema(implementation = InvoiceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    public InvoiceResponse getInvoice(@PathVariable Long invoiceId) throws DomainException.ResourceNotFoundException {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new DomainException.ResourceNotFoundException("Invoice not found with id: " + invoiceId));
        return invoiceMapper.toResponse(invoice);
    }

    @PostMapping("/issue/{orderId}")
    @Operation(summary = "Issue an invoice for an order",
            description = "Creates and issues an invoice for a confirmed order. The invoice amount matches the order total.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invoice issued",
                    content = @Content(schema = @Schema(implementation = InvoiceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "400", description = "Invoice already exists for this order or order is not CONFIRMED/BILLED")
    })
    public InvoiceResponse issueInvoice(@PathVariable Long orderId) throws DomainException {
        Invoice invoice = issueInvoiceUseCase.execute(orderId);
        return invoiceMapper.toResponse(invoice);
    }

    @GetMapping
    @Operation(summary = "List invoices",
            description = "Returns a paginated list of invoices optionally filtered by customer ID and/or status.")
    @ApiResponse(responseCode = "200", description = "Paginated list of invoices",
            content = @Content(schema = @Schema(implementation = PageResponse.class)))
    public PageResponse<InvoiceResponse> getInvoices(
            @Parameter(description = "Filter by customer ID") @RequestParam(required = false) Long customerId,
            @Parameter(description = "Filter by invoice status") @RequestParam(required = false) InvoiceStatus status,
            @Parameter(description = "Pagination and sorting") @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        org.springframework.data.domain.Page<Invoice> invoicesPage;
        if (customerId != null && status != null) {
            invoicesPage = listInvoicesUseCase.listByCustomerIdAndStatus(customerId, status, pageable);
        } else if (customerId != null) {
            invoicesPage = listInvoicesUseCase.listByCustomerId(customerId, pageable);
        } else if (status != null) {
            invoicesPage = listInvoicesUseCase.listByStatus(status, pageable);
        } else {
            invoicesPage = listInvoicesUseCase.listAll(pageable);
        }
        return PageResponse.from(invoicesPage, invoicesPage.map(invoiceMapper::toResponse).getContent());
    }
}
