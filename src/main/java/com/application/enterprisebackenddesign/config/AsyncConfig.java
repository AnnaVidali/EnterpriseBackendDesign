package com.application.enterprisebackenddesign.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

/**
 * Enables Spring's asynchronous method execution capability.
 *
 * Interview context: @EnableAsync activates Spring's proxy-based async
 * mechanism. When a bean method annotated with @Async is called, Spring
 * submits it to a TaskExecutor (a SimpleAsyncTaskExecutor by default)
 * and returns immediately to the caller.
 *
 * In this project, all domain event handlers are @Async. When an order
 * is created, the response returns to the client immediately, while
 * the warehouse notification and analytics tracking run in a background
 * thread. This keeps API response times fast and isolates side-effect
 * failures from the main business logic.
 *
 * For production, configure a ThreadPoolTaskExecutor with bounded queues
 * and a rejection policy to prevent unbounded thread growth under load.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
