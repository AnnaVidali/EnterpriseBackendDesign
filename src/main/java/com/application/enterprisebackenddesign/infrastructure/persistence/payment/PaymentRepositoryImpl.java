package com.application.enterprisebackenddesign.infrastructure.persistence.payment;

import com.application.enterprisebackenddesign.domain.payment.Payment;
import com.application.enterprisebackenddesign.domain.payment.PaymentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PaymentRepositoryImpl implements PaymentRepository {
    @Override
    public Payment save(Payment payment) {
        return null;
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<Payment> findByInvoiceId(Long invoiceId) {
        return List.of();
    }
}
