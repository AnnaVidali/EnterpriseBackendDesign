package com.application.enterprisebackenddesign.infrastructure.persistence.payment;

import com.application.enterprisebackenddesign.domain.payment.Payment;
import com.application.enterprisebackenddesign.domain.payment.PaymentRepository;
import com.application.enterprisebackenddesign.domain.payment.PaymentStatus;
import com.application.enterprisebackenddesign.infrastructure.persistence.payment.entity.PaymentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Adapter implementation of the PaymentRepository port.
 *
 * Hexagonal Architecture: Implements PaymentRepository using Spring Data JPA.
 * The domain Payment aggregate is mapped to/from PaymentEntity via PaymentMapper.
 *
 * This is the simplest repository adapter — PaymentEntity has no child
 * entities and no special cascade behavior. The save/find pattern is
 * straightforward.
 */
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
    public List<Payment> findAll() {
        return paymentRepository.findAll().stream()
                .map(paymentMapper::toDomain)
                .toList();
    }

    @Override
    public Page<Payment> findAll(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(paymentMapper::toDomain);
    }

    @Override
    public List<Payment> findByInvoiceId(Long invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId).stream().map(paymentMapper::toDomain).toList();
    }

    @Override
    public Page<Payment> findByInvoiceId(Long invoiceId, Pageable pageable) {
        return paymentRepository.findByInvoiceId(invoiceId, pageable).map(paymentMapper::toDomain);
    }

    @Override
    public List<Payment> findByCustomerId(Long customerId) {
        return paymentRepository.findByCustomerId(customerId).stream().map(paymentMapper::toDomain).toList();
    }

    @Override
    public Page<Payment> findByCustomerId(Long customerId, Pageable pageable) {
        return paymentRepository.findByCustomerId(customerId, pageable).map(paymentMapper::toDomain);
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status).stream().map(paymentMapper::toDomain).toList();
    }

    @Override
    public Page<Payment> findByStatus(PaymentStatus status, Pageable pageable) {
        return paymentRepository.findByStatus(status, pageable).map(paymentMapper::toDomain);
    }

    @Override
    public List<Payment> findByInvoiceIdAndCustomerIdAndStatus(Long invoiceId, Long customerId, PaymentStatus status) {
        return paymentRepository.findByInvoiceIdAndCustomerIdAndStatus(invoiceId, customerId, status).stream().map(paymentMapper::toDomain).toList();
    }

    @Override
    public Page<Payment> findByInvoiceIdAndCustomerIdAndStatus(Long invoiceId, Long customerId, PaymentStatus status, Pageable pageable) {
        return paymentRepository.findByInvoiceIdAndCustomerIdAndStatus(invoiceId, customerId, status, pageable).map(paymentMapper::toDomain);
    }

}
