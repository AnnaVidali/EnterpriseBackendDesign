package com.application.enterprisebackenddesign.application.payment;

import com.application.enterprisebackenddesign.domain.payment.Payment;
import com.application.enterprisebackenddesign.domain.payment.PaymentRepository;
import com.application.enterprisebackenddesign.domain.payment.PaymentStatus;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ListPaymentsUseCase {

    private final PaymentRepository paymentRepository;

    public ListPaymentsUseCase(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List<Payment> listAll() {
        return paymentRepository.findAll();
    }

    public List<Payment> listByInvoiceId(Long id) {
        return paymentRepository.findByInvoiceId(id);
    }

    public List<Payment> listByCustomerId(Long id) {
        return paymentRepository.findByCustomerId(id);
    }

    public List<Payment> listByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    public List<Payment> listByInvoiceIdAndCustomerIdAndStatus(Long invoiceId, Long customerId, PaymentStatus status) {
        return paymentRepository.findByInvoiceIdAndCustomerIdAndStatus(invoiceId, customerId, status);
    }
}
