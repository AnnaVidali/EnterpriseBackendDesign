package com.application.enterprisebackenddesign.application.invoice;

import com.application.enterprisebackenddesign.domain.invoice.Invoice;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceRepository;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceStatus;
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
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListInvoicesUseCaseTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Mock
    private InvoiceRepository invoiceRepository;
    @InjectMocks
    private ListInvoicesUseCase useCase;

    private Money amount() throws DomainException {
        return new Money(new BigDecimal("100.00"), USD);
    }

    @Test
    void shouldReturnAllInvoices() throws DomainException {
        List<Invoice> invoices = List.of(
                new Invoice(1L, 1L, 1L, amount(), InvoiceStatus.DRAFT, null),
                new Invoice(2L, 2L, 2L, amount(), InvoiceStatus.ISSUED, LocalDateTime.now())
        );
        when(invoiceRepository.findAll()).thenReturn(invoices);

        List<Invoice> result = useCase.listAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldReturnPagedInvoices() throws DomainException {
        List<Invoice> invoices = List.of(new Invoice(1L, 1L, 1L, amount(), InvoiceStatus.DRAFT, null));
        Pageable pageable = PageRequest.of(0, 20);
        when(invoiceRepository.findAll(pageable)).thenReturn(new PageImpl<>(invoices, pageable, 1));

        Page<Invoice> result = useCase.listAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldReturnInvoicesByCustomerId() throws DomainException {
        List<Invoice> invoices = List.of(new Invoice(1L, 1L, 1L, amount(), InvoiceStatus.DRAFT, null));
        when(invoiceRepository.findByCustomerId(1L)).thenReturn(invoices);

        List<Invoice> result = useCase.listByCustomerId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldReturnPagedInvoicesByCustomerId() throws DomainException {
        List<Invoice> invoices = List.of(new Invoice(1L, 1L, 1L, amount(), InvoiceStatus.DRAFT, null));
        Pageable pageable = PageRequest.of(0, 20);
        when(invoiceRepository.findByCustomerId(1L, pageable)).thenReturn(new PageImpl<>(invoices, pageable, 1));

        Page<Invoice> result = useCase.listByCustomerId(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldReturnInvoicesByStatus() throws DomainException {
        List<Invoice> invoices = List.of(new Invoice(1L, 1L, 1L, amount(), InvoiceStatus.DRAFT, null));
        when(invoiceRepository.findByStatus(InvoiceStatus.DRAFT)).thenReturn(invoices);

        List<Invoice> result = useCase.listByStatus(InvoiceStatus.DRAFT);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldReturnPagedInvoicesByStatus() throws DomainException {
        List<Invoice> invoices = List.of(new Invoice(1L, 1L, 1L, amount(), InvoiceStatus.DRAFT, null));
        Pageable pageable = PageRequest.of(0, 20);
        when(invoiceRepository.findByStatus(InvoiceStatus.DRAFT, pageable)).thenReturn(new PageImpl<>(invoices, pageable, 1));

        Page<Invoice> result = useCase.listByStatus(InvoiceStatus.DRAFT, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldReturnInvoicesByCustomerIdAndStatus() throws DomainException {
        List<Invoice> invoices = List.of(new Invoice(1L, 1L, 1L, amount(), InvoiceStatus.DRAFT, null));
        when(invoiceRepository.findByCustomerIdAndStatus(1L, InvoiceStatus.DRAFT)).thenReturn(invoices);

        List<Invoice> result = useCase.listByCustomerIdAndStatus(1L, InvoiceStatus.DRAFT);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldReturnPagedInvoicesByCustomerIdAndStatus() throws DomainException {
        List<Invoice> invoices = List.of(new Invoice(1L, 1L, 1L, amount(), InvoiceStatus.DRAFT, null));
        Pageable pageable = PageRequest.of(0, 20);
        when(invoiceRepository.findByCustomerIdAndStatus(1L, InvoiceStatus.DRAFT, pageable))
                .thenReturn(new PageImpl<>(invoices, pageable, 1));

        Page<Invoice> result = useCase.listByCustomerIdAndStatus(1L, InvoiceStatus.DRAFT, pageable);

        assertThat(result.getContent()).hasSize(1);
    }
}
