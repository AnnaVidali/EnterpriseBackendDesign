package com.application.enterprisebackenddesign.infrastructure.persistence.customer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * JPA entity for the customers table.
 *
 * Interview context: This is the PERSISTENCE model, separate from the DOMAIN model (Customer).
 * Key mapping decisions:
 * 1. No @GeneratedValue — IDs are assigned by the application via IdGenerator.
 *    This avoids coupling domain object creation to database round-trips.
 * 2. @Version for optimistic locking — prevents lost updates when two users
 *    modify the same customer simultaneously. Hibernate increments version
 *    on every UPDATE and checks it hasn't changed since the last READ.
 * 3. @CreatedDate/@LastModifiedDate — auto-populated by AuditingEntityListener,
 *    configured via @EnableJpaAuditing in JpaConfig.
 * 4. No JPA relationships to other entities — foreign keys are stored as
 *    simple Long fields (customerId, etc.), avoiding lazy loading issues
 *    and keeping aggregate boundaries clean. This is a deliberate choice
 *    over @ManyToOne for cross-aggregate references.
 */
@Getter
@Setter
@Entity
@Table(name = "customers")
@EntityListeners(AuditingEntityListener.class)
public class CustomerEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime lastModifiedDate;

    @Version
    private Long version = 0L;
}
