package com.application.enterprisebackenddesign.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing for automatic timestamping of entities.
 *
 * Interview context: @EnableJpaAuditing activates the AuditingEntityListener.
 * Entities annotated with @EntityListeners(AuditingEntityListener.class) get
 * their @CreatedDate and @LastModifiedDate fields populated automatically
 * on persist and update — no manual timestamp management needed.
 *
 * This is configured as a separate @Configuration class because:
 * 1. It requires the AuditingEntityListener to be registered as a Spring bean.
 * 2. It's cleaner than adding @EnableJpaAuditing to the main application class.
 * 3. In tests, we can exclude this config if we don't need auditing.
 *
 * Combined with @Version for optimistic locking, these audit fields provide
 * both concurrency control and a basic audit trail without extra code.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
