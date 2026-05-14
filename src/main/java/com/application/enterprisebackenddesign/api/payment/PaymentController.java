package com.application.enterprisebackenddesign.api.payment;

import com.application.enterprisebackenddesign.application.payment.ListPaymentsUseCase;
import com.application.enterprisebackenddesign.application.payment.ProcessPaymentUseCase;
import com.application.enterprisebackenddesign.domain.payment.Payment;
import com.application.enterprisebackenddesign.domain.payment.PaymentRepository;
import com.application.enterprisebackenddesign.domain.payment.PaymentStatus;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
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
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) throws DomainException {
        Payment payment = processPaymentUseCase.execute(request.invoiceId(), paymentMapper.toMoney(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentMapper.toResponse(payment));
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse getPayment(@PathVariable Long paymentId) throws DomainException.ResourceNotFoundException {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new DomainException.ResourceNotFoundException("Payment not found with id: " + paymentId));
        return paymentMapper.toResponse(payment);
    }

    @GetMapping
    public List<PaymentResponse> getPayments(@RequestParam(required = false) Long invoiceId, @RequestParam(required = false) Long customerId, @RequestParam(required = false) PaymentStatus status) {
        List<Payment> payments;
        if (invoiceId != null && customerId != null && status != null) {
            payments = listPaymentsUseCase.listByInvoiceIdAndCustomerIdAndStatus(invoiceId, customerId, status);
        } else if (invoiceId != null) {
            payments = listPaymentsUseCase.listByInvoiceId(invoiceId);
        }else if (customerId != null) {
            payments = listPaymentsUseCase.listByCustomerId(customerId);
        } else if (status != null) {
            payments = listPaymentsUseCase.listByStatus(status);
        } else {
            payments = listPaymentsUseCase.listAll();
        }
        return payments.stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }
}
