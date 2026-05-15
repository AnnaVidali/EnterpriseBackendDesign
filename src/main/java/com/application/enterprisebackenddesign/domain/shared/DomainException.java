package com.application.enterprisebackenddesign.domain.shared;

/**
 * Base exception for all domain-level errors.
 * Uses typed subclasses to distinguish between business rule violations
 * and resource-not-found scenarios, allowing callers to handle each case appropriately.
 *
 * Interview context: Domain exceptions represent failures of business rules,
 * NOT technical failures. They use checked exceptions to force callers (use cases)
 * to explicitly handle business rule violations rather than letting them propagate
 * as runtime exceptions.
 *
 * Why checked? Domain validation is part of the business contract — if a method
 * says "throws BusinessRuleViolationException", the caller cannot silently ignore
 * that the operation might fail. The compiler enforces handling, which is appropriate
 * for domain logic where skipping validation is a bug.
 */
public class DomainException extends Exception {

    public DomainException(String message) {
        super(message);
    }

    public static class BusinessRuleViolationException extends DomainException {
        public BusinessRuleViolationException(String message) {
            super(message);
        }
    }

    public static class ResourceNotFoundException extends DomainException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

}
