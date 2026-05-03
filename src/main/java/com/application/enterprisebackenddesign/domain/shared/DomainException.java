package com.application.enterprisebackenddesign.domain.shared;

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

    public static class InvalidStateException extends DomainException {
        public InvalidStateException(String message) {
            super(message);
        }
    }

}
