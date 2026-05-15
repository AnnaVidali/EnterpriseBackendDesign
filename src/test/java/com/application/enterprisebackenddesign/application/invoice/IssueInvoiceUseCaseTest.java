package com.application.enterprisebackenddesign.application.invoice;

import com.application.enterprisebackenddesign.application.shared.DomainEventPublisher;
import com.application.enterprisebackenddesign.domain.invoice.Invoice;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceRepository;
import com.application.enterprisebackenddesign.domain.invoice.InvoiceStatus;
import com.application.enterprisebackenddesign.domain.order.Order;
import com.application.enterprisebackenddesign.domain.order.OrderLine;
import com.application.enterprisebackenddesign.domain.order.OrderRepository;
import com.application.enterprisebackenddesign.domain.order.OrderStatus;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueInvoiceUseCaseTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private DomainEventPublisher eventPublisher;
    @InjectMocks
    private IssueInvoiceUseCase useCase;

    @Captor
    private ArgumentCaptor<Invoice> invoiceCaptor;

    private Order confirmedOrder() throws DomainException {
        Money price = new Money(new BigDecimal("100.00"), USD);
        OrderLine line = new OrderLine(1L, 10L, 2, price);
        Order order = new Order(1L, 1L, List.of(line), USD);
        order.confirmOrder();
        return order;
    }

    @Test
    void shouldCreateAndIssueInvoiceForConfirmedOrder() throws DomainException {
        Order order = confirmedOrder();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(invoiceRepository.findByOrderId(1L)).thenReturn(List.of());
        when(invoiceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Invoice result = useCase.execute(1L);

        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
        assertThat(result.getOrderId()).isEqualTo(1L);
        assertThat(result.getCustomerId()).isEqualTo(1L);
        verify(invoiceRepository).save(invoiceCaptor.capture());
        assertThat(invoiceCaptor.getValue().getStatus()).isEqualTo(InvoiceStatus.ISSUED);
        verify(eventPublisher, atLeastOnce()).publish(any());
    }

    @Test
    void shouldReuseExistingDraftInvoice() throws DomainException {
        Order order = confirmedOrder();
        Invoice draftInvoice = Invoice.fromOrder(5L, 1L, 1L, order.getTotalAmount());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(invoiceRepository.findByOrderId(1L)).thenReturn(List.of(draftInvoice));
        when(invoiceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Invoice result = useCase.execute(1L);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
    }

    @Test
    void shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(99L))
                .isInstanceOf(DomainException.ResourceNotFoundException.class);
        verifyNoInteractions(invoiceRepository, eventPublisher);
    }

    @Test
    void shouldThrowWhenOrderNotConfirmed() throws DomainException {
        Money price = new Money(BigDecimal.TEN, USD);
        OrderLine line = new OrderLine(1L, 10L, 2, price);
        Order order = new Order(1L, 1L, List.of(line), USD);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> useCase.execute(1L))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Order status is not CONFIRMED");
        verifyNoInteractions(invoiceRepository);
    }

    @Test
    void shouldIgnorePaidInvoiceWhenLookingForExistingDraft() throws DomainException {
        Order order = confirmedOrder();
        LocalDateTime now = LocalDateTime.now();
        Invoice paidInvoice = new Invoice(5L, 1L, 1L, order.getTotalAmount(), InvoiceStatus.PAID, now);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(invoiceRepository.findByOrderId(1L)).thenReturn(List.of(paidInvoice));
        when(invoiceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Invoice result = useCase.execute(1L);

        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
        assertThat(result.getId()).isNotEqualTo(5L);
    }
}
