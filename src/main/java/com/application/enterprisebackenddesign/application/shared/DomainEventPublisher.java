package com.application.enterprisebackenddesign.application.shared;

import com.application.enterprisebackenddesign.domain.shared.DomainEvent;

public interface DomainEventPublisher {

    void publish(DomainEvent event);
}
