package com.application.enterprisebackenddesign.api.payment;

import com.application.enterprisebackenddesign.api.shared.PageResponse;
import com.application.enterprisebackenddesign.application.payment.ListPaymentsUseCase;
import com.application.enterprisebackenddesign.application.payment.ProcessPaymentUseCase;
import com.application.enterprisebackenddesign.domain.payment.Payment;
import com.application.enterprisebackenddesign.domain.payment.PaymentRepository;
import com.application.enterprisebackenddesign.domain.payment.PaymentStatus;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Payment processing — process payments for invoices and retrieve payment history")
public class PaymentController {

    private final ProcessPaymentUseCase processPaymentUseCase;
    private final ListPaymentsUseCase listPaymentsUseCase;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    public PaymentController(ProcessPaymentUseCase processPaymentUseCase, ListPaymentsUseCase listPaymentsUseCase,
                             PaymentRepository paymentRepository,
                             PaymentMapper paymentMapper) {
        this.processPaymentUseCase = processPaymentUseCase;
        this.listPaymentsUseCase = listPaymentsUseCase;
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
    }

    @PostMapping
    @Operation(summary = "Process a payment",
            description = "Processes a payment for an invoice. Validates the invoice status, matches the amount, " +
                    "calls the configured payment gateway, and records the result.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment processed",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request, invoice not ISSUED, or amount mismatch"),
            @ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) throws DomainException {
        Payment payment = processPaymentUseCase.execute(request.invoiceId(), paymentMapper.toMoney(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentMapper.toResponse(payment));
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get a payment by ID",
            description = "Returns payment details including status, amount, and gateway transaction ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment found",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public PaymentResponse getPayment(@PathVariable Long paymentId) throws DomainException.ResourceNotFoundException {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new DomainException.ResourceNotFoundException("Payment not found with id: " + paymentId));
        return paymentMapper.toResponse(payment);
    }

    @GetMapping
    @Operation(summary = "List payments",
            description = "Returns a paginated list of payments optionally filtered by invoice ID, customer ID, and/or status.")
    @ApiResponse(responseCode = "200", description = "Paginated list of payments",
            content = @Content(schema = @Schema(implementation = PageResponse.class)))
    public PageResponse<PaymentResponse> getPayments(
            @Parameter(description = "Filter by invoice ID") @RequestParam(required = false) Long invoiceId,
            @Parameter(description = "Filter by customer ID") @RequestParam(required = false) Long customerId,
            @Parameter(description = "Filter by payment status") @RequestParam(required = false) PaymentStatus status,
            @Parameter(description = "Pagination and sorting") @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        org.springframework.data.domain.Page<Payment> paymentsPage;
        if (invoiceId != null && customerId != null && status != null) {
            paymentsPage = listPaymentsUseCase.listByInvoiceIdAndCustomerIdAndStatus(invoiceId, customerId, status, pageable);
        } else if (invoiceId != null) {
            paymentsPage = listPaymentsUseCase.listByInvoiceId(invoiceId, pageable);
        } else if (customerId != null) {
            paymentsPage = listPaymentsUseCase.listByCustomerId(customerId, pageable);
        } else if (status != null) {
            paymentsPage = listPaymentsUseCase.listByStatus(status, pageable);
        } else {
            paymentsPage = listPaymentsUseCase.listAll(pageable);
        }
        return PageResponse.from(paymentsPage, paymentsPage.map(paymentMapper::toResponse).getContent());
    }
}
