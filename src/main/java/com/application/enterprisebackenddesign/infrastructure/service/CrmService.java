package com.application.enterprisebackenddesign.infrastructure.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CrmService {
    private static final Logger log = LoggerFactory.getLogger(CrmService.class);

    public void syncCustomer(Long customerId, String name, String email) {
        log.info("Syncing customer to CRM: id={}, name={}, email={}", customerId, name, email);
    }

    public void updateOrderStatus(Long orderId, String status) {
        log.info("Updating order status in CRM: orderId={}, status={}", orderId, status);
    }
}
