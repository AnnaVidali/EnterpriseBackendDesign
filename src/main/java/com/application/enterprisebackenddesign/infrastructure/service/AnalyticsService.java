package com.application.enterprisebackenddesign.infrastructure.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {
    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    public void trackOrderCreated(Long orderId, Long customerId, int lineCount) {
        log.info("Tracking order creation in analytics: orderId={}, customerId={}, lines={}", orderId, customerId, lineCount);
    }

    public void trackPayment(Long paymentId, double amount, String status) {
        log.info("Tracking payment in analytics: paymentId={}, amount={}, status={}", paymentId, amount, status);
    }
}
