package com.application.enterprisebackenddesign.application.payment;

import com.application.enterprisebackenddesign.application.shared.DomainEventPublisher;
import com.application.enterprisebackenddesign.domain.invoice.Invoice;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceRepository;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceStatus;
import com.application.enterprisebackenddesign.domain.payment.Payment;
import com.application.enterprisebackenddesign.domain.payment.PaymentRepository;
import com.application.enterprisebackenddesign.domain.payment.PaymentStatus;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import com.application.enterprisebackenddesign.infrastructure.external.GatewayPaymentRequest;
import com.application.enterprisebackenddesign.infrastructure.external.PaymentGateway;
import com.application.enterprisebackenddesign.infrastructure.external.PaymentResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessPaymentUseCaseTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private DomainEventPublisher eventPublisher;
    @Mock
    private PaymentGateway paymentGateway;
    @InjectMocks
    private ProcessPaymentUseCase useCase;

    @Captor
    private ArgumentCaptor<Payment> paymentCaptor;

    private Invoice issuedInvoice() throws DomainException {
        Money amount = new Money(new BigDecimal("100.00"), USD);
        Invoice invoice = new Invoice(1L, 1L, 10L, amount, InvoiceStatus.DRAFT, null);
        invoice.issue();
        return invoice;
    }

    @Test
    void shouldProcessPaymentSuccessfully() throws DomainException {
        Invoice invoice = issuedInvoice();
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(paymentGateway.process(any(GatewayPaymentRequest.class)))
                .thenReturn(new PaymentResult(true, "txn-123", "Success"));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(invoiceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = useCase.execute(1L, new Money(new BigDecimal("100.00"), USD));

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(result.getPaymentDate()).isNotNull();
        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(eventPublisher, atLeast(2)).publish(any());
    }

    @Test
    void shouldHandlePaymentFailure() throws DomainException {
        Invoice invoice = issuedInvoice();
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(paymentGateway.process(any(GatewayPaymentRequest.class)))
                .thenReturn(new PaymentResult(false, null, "Insufficient funds"));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(invoiceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = useCase.execute(1L, new Money(new BigDecimal("100.00"), USD));

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void shouldThrowWhenInvoiceNotFound() {
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(99L, new Money(BigDecimal.TEN, USD)))
                .isInstanceOf(DomainException.ResourceNotFoundException.class);
        verifyNoInteractions(paymentGateway, paymentRepository, eventPublisher);
    }

    @Test
    void shouldThrowWhenInvoiceNotIssued() throws DomainException {
        Money amount = new Money(BigDecimal.TEN, USD);
        Invoice draftInvoice = new Invoice(1L, 1L, 10L, amount, InvoiceStatus.DRAFT, null);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(draftInvoice));
        assertThatThrownBy(() -> useCase.execute(1L, new Money(BigDecimal.TEN, USD)))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Invoice status is not ISSUED");
        verifyNoInteractions(paymentGateway);
    }

    @Test
    void shouldThrowWhenAmountDoesNotMatch() throws DomainException {
        Invoice invoice = issuedInvoice();
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        assertThatThrownBy(() -> useCase.execute(1L, new Money(new BigDecimal("50.00"), USD)))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Payment amount does not match");
        verifyNoInteractions(paymentGateway);
    }
}
