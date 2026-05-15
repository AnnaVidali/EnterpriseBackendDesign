package com.application.enterprisebackenddesign.api.shared;

import com.application.enterprisebackenddesign.domain.shared.DomainException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;

/**
 * Centralized exception handler — the error boundary between domain and HTTP.
 *
 * Hexagonal Architecture (Adapter layer concern): This @RestControllerAdvice
 * maps domain exceptions to HTTP status codes, keeping HTTP concerns out of
 * the domain layer. The domain layer throws only DomainException subclasses;
 * this handler decides whether they become 404 (ResourceNotFound), 400
 * (BusinessRuleViolation), or 409 (DataIntegrityViolation).
 *
 * Key design decisions:
 * - DomainExceptions map to 4xx status codes (client errors), never 5xx
 * - Infrastructure exceptions (DataIntegrityViolation) are logged at WARN
 *   and mapped to 409 CONFLICT to avoid leaking database internals
 * - Unhandled exceptions (the catch-all) return 500 with a generic message
 *   to avoid information leakage — the full stack trace is server-logged only
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DomainException.ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(DomainException.ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DomainException.BusinessRuleViolationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(DomainException.BusinessRuleViolationException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(HttpServletRequest request) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.NOT_FOUND, "Endpoint not found", request);
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorResponse.FieldErrorDetail> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldErrorDetail(
                        fe.getField(),
                        fe.getRejectedValue() != null ? fe.getRejectedValue().toString() : null,
                        fe.getDefaultMessage()))
                .toList();
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed",
                request.getRequestURI(),
                Instant.now(),
                fieldErrors
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.BAD_REQUEST,
                "Malformed JSON request body: " + ex.getMostSpecificCause().getMessage(), request);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<ErrorResponse.FieldErrorDetail> fieldErrors = ex.getConstraintViolations().stream()
                .map(cv -> new ErrorResponse.FieldErrorDetail(
                        cv.getPropertyPath().toString(),
                        cv.getInvalidValue() != null ? cv.getInvalidValue().toString() : null,
                        cv.getMessage()))
                .toList();
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Constraint validation failed",
                request.getRequestURI(),
                Instant.now(),
                fieldErrors
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.CONFLICT,
                "Database constraint violation: " + ex.getMostSpecificCause().getMessage(), request);
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error occurred", ex);
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ErrorResponse buildErrorResponse(HttpStatus status, String message, HttpServletRequest request) {
        return new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                Instant.now()
        );
    }
}
