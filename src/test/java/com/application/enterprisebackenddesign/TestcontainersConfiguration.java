package com.application.enterprisebackenddesign;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers configuration for integration tests.
 *
 * Interview context: We use Testcontainers instead of H2 for integration tests
 * because H2 has subtle SQL dialect differences from PostgreSQL that can hide
 * bugs. For example:
 * - H2 allows certain invalid GROUP BY expressions that PostgreSQL rejects.
 * - H2's BOOLEAN type maps differently from PostgreSQL's boolean.
 * - Flyway migrations written for PostgreSQL may fail on H2.
 *
 * By running tests against a real PostgreSQL container, we catch these issues
 * before deployment. The @ServiceConnection annotation (Spring Boot 3.1+)
 * automatically configures spring.datasource.* properties from the container,
 * so the application connects without any manual property overrides.
 *
 * The Kafka container is also available for testing event publishing end-to-end.
 * Previously, we mocked DomainEventPublisher in tests, but with Testcontainers,
 * the real Kafka broker starts and events can be verified on actual topics.
 *
 * Docker is required to run these tests — they will fail with "Cannot connect
 * to Docker daemon" if Docker is not running.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    public KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("apache/kafka-native:latest"));
    }

    @Bean
    @ServiceConnection
    public PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres:latest"));
    }

}
