package com.application.enterprisebackenddesign.domain.customer;

import com.application.enterprisebackenddesign.domain.shared.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Aggregate root representing a customer in the domain.
 * Encapsulates customer identity, contact information, and domain events
 * triggered by state changes such as creation, update, or deletion.
 *
 * Interview context: Customer is a simpler aggregate — it has no child
 * entities (unlike Order which has OrderLines). It validates its own
 * invariants (email format, non-empty name) in the constructor and raises
 * events for every state change.
 *
 * The pullEvents/clear pattern: Domain events are collected in an in-memory
 * list as the aggregate mutates. After the use case successfully persists
 * the aggregate, it calls pullEvents(true) to atomically retrieve and clear
 * the events, then publishes them. If persistence fails (e.g., optimistic
 * lock exception, constraint violation), the events are never published —
 * preventing the classic "event published but transaction rolled back" bug.
 *
 * This is NOT event sourcing — we store the current state, not the event
 * stream. But the pattern is compatible with event sourcing if we later
 * want to add an event store.
 */
@Getter
public class Customer {

    private final Long id;
    private String name;
    private String lastName;
    private String email;
    private final List<DomainEvent> events = new ArrayList<>();

    /**
     * @param id       unique identifier, must not be null
     * @param name     customer first name, must not be null or empty
     * @param lastName customer last name, must not be null or empty
     * @param email    customer email address, must match a valid email format
     * @throws DomainException.BusinessRuleViolationException if any validation fails
     */
    public Customer(Long id, String name, String lastName, String email) throws DomainException.BusinessRuleViolationException {
        if (id == null) {
            throw new DomainException.BusinessRuleViolationException("Id cannot be null.");
        }
        this.id = id;
        if (name == null || name.isEmpty()) {
            throw new DomainException.BusinessRuleViolationException("Name cannot be null or empty.");
        }
        this.name = name;
        if (lastName == null || lastName.isEmpty()) {
            throw new DomainException.BusinessRuleViolationException("Last name cannot be null or empty.");
        }
        this.lastName = lastName;
        if (!isEmailValid(email)) {
            throw new DomainException.BusinessRuleViolationException("Email address has invalid format.");
        }
        this.email = email;
        events.add(new CustomerCreatedEvent(this.id, this.name, this.lastName, this.email));
    }

    private Boolean isEmailValid(String email) {
        // Regular expression to match valid email formats
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        // Compile the regex
        Pattern p = Pattern.compile(emailRegex);
        // Check if email matches the pattern
        return email != null && p.matcher(email).matches();
    }

    public void updateEmail(String newEmail) throws DomainException {
        if (!isEmailValid(newEmail)) {
            throw new DomainException.BusinessRuleViolationException("Email address has invalid format.");
        }
        events.add(new CustomerUpdatedEvent(this.id, "email", this.email, newEmail));
        this.email = newEmail;
    }

    public void updateName(String newName) throws DomainException {
        if (newName == null || newName.isEmpty()) {
            throw new DomainException.BusinessRuleViolationException("Name cannot be null or empty.");
        }
        events.add(new CustomerUpdatedEvent(this.id, "name", this.name, newName));
        this.name = newName;
    }

    public void updateLastName(String newLastName) throws DomainException {
        if (newLastName == null || newLastName.isEmpty()) {
            throw new DomainException.BusinessRuleViolationException("Last name cannot be null or empty.");
        }
        events.add(new CustomerUpdatedEvent(this.id, "lastName", this.lastName, newLastName));
        this.lastName = newLastName;
    }

    public List<DomainEvent> pullEvents(boolean clear) {
        
        List<DomainEvent> copiedEvents = new ArrayList<>(events);
        if(clear) {
            events.clear();
        }
        return copiedEvents;
    }

    public void delete() {
        events.add(new CustomerDeletedEvent(this.id));
    }
}
