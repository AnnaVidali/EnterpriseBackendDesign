package com.application.enterprisebackenddesign.api.invoice;

import com.application.enterprisebackenddesign.application.invoice.IssueInvoiceUseCase;
import com.application.enterprisebackenddesign.application.invoice.ListInvoicesUseCase;
import com.application.enterprisebackenddesign.domain.invoice.Invoice;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceRepository;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceStatus;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
            description = "Returns invoices optionally filtered by customer ID and/or status.")
    @ApiResponse(responseCode = "200", description = "List of invoices",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = InvoiceResponse.class))))
    public List<InvoiceResponse> getInvoices(
            @Parameter(description = "Filter by customer ID") @RequestParam(required = false) Long customerId,
            @Parameter(description = "Filter by invoice status") @RequestParam(required = false) InvoiceStatus status) {
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
