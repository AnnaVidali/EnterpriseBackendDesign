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

}
