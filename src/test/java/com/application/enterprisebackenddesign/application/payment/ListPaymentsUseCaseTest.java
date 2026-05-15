package com.application.enterprisebackenddesign.application.payment;

import com.application.enterprisebackenddesign.domain.payment.Payment;
import com.application.enterprisebackenddesign.domain.payment.PaymentRepository;
import com.application.enterprisebackenddesign.domain.payment.PaymentStatus;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListPaymentsUseCaseTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Mock
    private PaymentRepository paymentRepository;
    @InjectMocks
    private ListPaymentsUseCase useCase;

    private Money amount() throws DomainException {
        return new Money(new BigDecimal("100.00"), USD);
    }

    @Test
    void shouldReturnAllPayments() throws DomainException {
        List<Payment> payments = List.of(
                new Payment(1L, 1L, 1L, 1L, amount(), PaymentStatus.COMPLETED),
                new Payment(2L, 2L, 2L, 2L, amount(), PaymentStatus.PENDING)
        );
        when(paymentRepository.findAll()).thenReturn(payments);

        List<Payment> result = useCase.listAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldReturnPagedPayments() throws DomainException {
        List<Payment> payments = List.of(new Payment(1L, 1L, 1L, 1L, amount(), PaymentStatus.COMPLETED));
        Pageable pageable = PageRequest.of(0, 20);
        when(paymentRepository.findAll(pageable)).thenReturn(new PageImpl<>(payments, pageable, 1));

        Page<Payment> result = useCase.listAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldReturnPaymentsByInvoiceId() throws DomainException {
        List<Payment> payments = List.of(new Payment(1L, 1L, 1L, 1L, amount(), PaymentStatus.COMPLETED));
        when(paymentRepository.findByInvoiceId(1L)).thenReturn(payments);

        List<Payment> result = useCase.listByInvoiceId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldReturnPagedPaymentsByInvoiceId() throws DomainException {
        List<Payment> payments = List.of(new Payment(1L, 1L, 1L, 1L, amount(), PaymentStatus.COMPLETED));
        Pageable pageable = PageRequest.of(0, 20);
        when(paymentRepository.findByInvoiceId(1L, pageable)).thenReturn(new PageImpl<>(payments, pageable, 1));

        Page<Payment> result = useCase.listByInvoiceId(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldReturnPaymentsByCustomerId() throws DomainException {
        List<Payment> payments = List.of(new Payment(1L, 1L, 1L, 1L, amount(), PaymentStatus.COMPLETED));
        when(paymentRepository.findByCustomerId(1L)).thenReturn(payments);

        List<Payment> result = useCase.listByCustomerId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldReturnPagedPaymentsByCustomerId() throws DomainException {
        List<Payment> payments = List.of(new Payment(1L, 1L, 1L, 1L, amount(), PaymentStatus.COMPLETED));
        Pageable pageable = PageRequest.of(0, 20);
        when(paymentRepository.findByCustomerId(1L, pageable)).thenReturn(new PageImpl<>(payments, pageable, 1));

        Page<Payment> result = useCase.listByCustomerId(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldReturnPaymentsByStatus() throws DomainException {
        List<Payment> payments = List.of(new Payment(1L, 1L, 1L, 1L, amount(), PaymentStatus.PENDING));
        when(paymentRepository.findByStatus(PaymentStatus.PENDING)).thenReturn(payments);

        List<Payment> result = useCase.listByStatus(PaymentStatus.PENDING);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldReturnPagedPaymentsByStatus() throws DomainException {
        List<Payment> payments = List.of(new Payment(1L, 1L, 1L, 1L, amount(), PaymentStatus.PENDING));
        Pageable pageable = PageRequest.of(0, 20);
        when(paymentRepository.findByStatus(PaymentStatus.PENDING, pageable)).thenReturn(new PageImpl<>(payments, pageable, 1));

        Page<Payment> result = useCase.listByStatus(PaymentStatus.PENDING, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldReturnPaymentsByInvoiceIdAndCustomerIdAndStatus() throws DomainException {
        List<Payment> payments = List.of(new Payment(1L, 1L, 1L, 1L, amount(), PaymentStatus.COMPLETED));
        when(paymentRepository.findByInvoiceIdAndCustomerIdAndStatus(1L, 1L, PaymentStatus.COMPLETED)).thenReturn(payments);

        List<Payment> result = useCase.listByInvoiceIdAndCustomerIdAndStatus(1L, 1L, PaymentStatus.COMPLETED);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldReturnPagedPaymentsByInvoiceIdAndCustomerIdAndStatus() throws DomainException {
        List<Payment> payments = List.of(new Payment(1L, 1L, 1L, 1L, amount(), PaymentStatus.COMPLETED));
        Pageable pageable = PageRequest.of(0, 20);
        when(paymentRepository.findByInvoiceIdAndCustomerIdAndStatus(1L, 1L, PaymentStatus.COMPLETED, pageable))
                .thenReturn(new PageImpl<>(payments, pageable, 1));

        Page<Payment> result = useCase.listByInvoiceIdAndCustomerIdAndStatus(1L, 1L, PaymentStatus.COMPLETED, pageable);

        assertThat(result.getContent()).hasSize(1);
    }
}
