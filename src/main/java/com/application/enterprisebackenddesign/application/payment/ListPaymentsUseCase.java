package com.application.enterprisebackenddesign.application.payment;

import com.application.enterprisebackenddesign.domain.payment.Payment;
import com.application.enterprisebackenddesign.domain.payment.PaymentRepository;
import com.application.enterprisebackenddesign.domain.payment.PaymentStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<Payment> listAll(Pageable pageable) {
        return paymentRepository.findAll(pageable);
    }

    public List<Payment> listByInvoiceId(Long id) {
        return paymentRepository.findByInvoiceId(id);
    }

    public Page<Payment> listByInvoiceId(Long id, Pageable pageable) {
        return paymentRepository.findByInvoiceId(id, pageable);
    }

    public List<Payment> listByCustomerId(Long id) {
        return paymentRepository.findByCustomerId(id);
    }

    public Page<Payment> listByCustomerId(Long id, Pageable pageable) {
        return paymentRepository.findByCustomerId(id, pageable);
    }

    public List<Payment> listByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    public Page<Payment> listByStatus(PaymentStatus status, Pageable pageable) {
        return paymentRepository.findByStatus(status, pageable);
    }

    public List<Payment> listByInvoiceIdAndCustomerIdAndStatus(Long invoiceId, Long customerId, PaymentStatus status) {
        return paymentRepository.findByInvoiceIdAndCustomerIdAndStatus(invoiceId, customerId, status);
    }

    public Page<Payment> listByInvoiceIdAndCustomerIdAndStatus(Long invoiceId, Long customerId, PaymentStatus status, Pageable pageable) {
        return paymentRepository.findByInvoiceIdAndCustomerIdAndStatus(invoiceId, customerId, status, pageable);
    }
}
