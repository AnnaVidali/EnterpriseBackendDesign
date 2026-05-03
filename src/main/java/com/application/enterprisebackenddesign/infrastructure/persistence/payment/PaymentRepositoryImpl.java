package com.application.enterprisebackenddesign.infrastructure.persistence.payment;

import com.application.enterprisebackenddesign.domain.payment.Payment;
import com.application.enterprisebackenddesign.domain.payment.PaymentRepository;
import com.application.enterprisebackenddesign.infrastructure.persistence.payment.entity.PaymentEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PaymentRepositoryImpl implements PaymentRepository {

    private final SpringDataPaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    public PaymentRepositoryImpl(SpringDataPaymentRepository paymentRepository, PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = paymentMapper.toEntity(payment);
        PaymentEntity saved = paymentRepository.save(entity);
        return paymentMapper.toDomain(saved);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return paymentRepository.findById(id).map(paymentMapper::toDomain);
    }

    @Override
    public List<Payment> findByInvoiceId(Long invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId).stream().map(paymentMapper::toDomain).toList();
    }
}
