package com.application.enterprisebackenddesign.infrastructure.events.handlers;

import com.application.enterprisebackenddesign.domain.shared.CustomerCreatedEvent;
import com.application.enterprisebackenddesign.infrastructure.service.CrmService;
import com.application.enterprisebackenddesign.infrastructure.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class CustomerCreatedEventHandler {
    private static final Logger log = LoggerFactory.getLogger(CustomerCreatedEventHandler.class);
    private final EmailService emailService;
    private final CrmService crmService;

    public CustomerCreatedEventHandler(EmailService emailService, CrmService crmService) {
        this.emailService = emailService;
        this.crmService = crmService;
    }

    @Async
    @EventListener
    public void handle(CustomerCreatedEvent event) {
        log.info("Handling customer created event: id={}, email={}", event.getId(), event.getEmail());
        emailService.sendEmail(
                event.getEmail(),
                "Welcome to Enterprise Platform",
                "Hi " + event.getName() + ", welcome to our platform!"
        );
        crmService.syncCustomer(event.getId(), event.getName() + " " + event.getLastName(), event.getEmail());
    }
}
