package com.application.enterprisebackenddesign.domain.customer;

import com.application.enterprisebackenddesign.domain.shared.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerTest {

    @Test
    void shouldCreateCustomer() throws DomainException {
        Customer customer = new Customer(1L, "John", "Doe", "john@example.com");
        assertThat(customer.getId()).isEqualTo(1L);
        assertThat(customer.getName()).isEqualTo("John");
        assertThat(customer.getLastName()).isEqualTo("Doe");
        assertThat(customer.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void shouldEmitCustomerCreatedEvent() throws DomainException {
        Customer customer = new Customer(1L, "John", "Doe", "john@example.com");
        List<DomainEvent> events = customer.pullEvents(false);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(CustomerCreatedEvent.class);
        CustomerCreatedEvent event = (CustomerCreatedEvent) events.get(0);
        assertThat(event.getName()).isEqualTo("John");
        assertThat(event.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void shouldRejectNullId() {
        assertThatThrownBy(() -> new Customer(null, "John", "Doe", "john@example.com"))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Id cannot be null");
    }

    @Test
    void shouldRejectNullName() {
        assertThatThrownBy(() -> new Customer(1L, null, "Doe", "john@example.com"))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Name cannot be null or empty");
    }

    @Test
    void shouldRejectEmptyName() {
        assertThatThrownBy(() -> new Customer(1L, "", "Doe", "john@example.com"))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Name cannot be null or empty");
    }

    @Test
    void shouldRejectNullLastName() {
        assertThatThrownBy(() -> new Customer(1L, "John", null, "john@example.com"))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Last name cannot be null or empty");
    }

    @Test
    void shouldRejectEmptyLastName() {
        assertThatThrownBy(() -> new Customer(1L, "John", "", "john@example.com"))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Last name cannot be null or empty");
    }

    @Test
    void shouldRejectInvalidEmailWithoutAt() {
        assertThatThrownBy(() -> new Customer(1L, "John", "Doe", "notanemail"))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Email address has invalid format");
    }

    @Test
    void shouldRejectInvalidEmailWithoutDomain() {
        assertThatThrownBy(() -> new Customer(1L, "John", "Doe", "user@.com"))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Email address has invalid format");
    }

    @Test
    void shouldRejectNullEmail() {
        assertThatThrownBy(() -> new Customer(1L, "John", "Doe", null))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Email address has invalid format");
    }

    @Test
    void shouldUpdateEmail() throws DomainException {
        Customer customer = new Customer(1L, "John", "Doe", "john@example.com");
        customer.pullEvents(true);
        customer.updateEmail("newemail@example.com");
        assertThat(customer.getEmail()).isEqualTo("newemail@example.com");
    }

    @Test
    void shouldEmitCustomerUpdatedEventOnEmailChange() throws DomainException {
        Customer customer = new Customer(1L, "John", "Doe", "john@example.com");
        customer.pullEvents(true);
        customer.updateEmail("new@example.com");
        List<DomainEvent> events = customer.pullEvents(false);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(CustomerUpdatedEvent.class);
        CustomerUpdatedEvent event = (CustomerUpdatedEvent) events.get(0);
        assertThat(event.getFieldName()).isEqualTo("email");
        assertThat(event.getOldValue()).isEqualTo("john@example.com");
        assertThat(event.getNewValue()).isEqualTo("new@example.com");
    }

    @Test
    void shouldRejectUpdateToInvalidEmail() throws DomainException {
        Customer customer = new Customer(1L, "John", "Doe", "john@example.com");
        assertThatThrownBy(() -> customer.updateEmail("invalid"))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Email address has invalid format");
    }

    @Test
    void shouldUpdateName() throws DomainException {
        Customer customer = new Customer(1L, "John", "Doe", "john@example.com");
        customer.pullEvents(true);
        customer.updateName("Jane");
        assertThat(customer.getName()).isEqualTo("Jane");
    }

    @Test
    void shouldEmitCustomerUpdatedEventOnNameChange() throws DomainException {
        Customer customer = new Customer(1L, "John", "Doe", "john@example.com");
        customer.pullEvents(true);
        customer.updateName("Jane");
        List<DomainEvent> events = customer.pullEvents(false);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(CustomerUpdatedEvent.class);
        CustomerUpdatedEvent event = (CustomerUpdatedEvent) events.get(0);
        assertThat(event.getFieldName()).isEqualTo("name");
    }

    @Test
    void shouldRejectUpdateToEmptyName() throws DomainException {
        Customer customer = new Customer(1L, "John", "Doe", "john@example.com");
        assertThatThrownBy(() -> customer.updateName(""))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Name cannot be null or empty");
    }

    @Test
    void shouldRejectUpdateToNullName() throws DomainException {
        Customer customer = new Customer(1L, "John", "Doe", "john@example.com");
        assertThatThrownBy(() -> customer.updateName(null))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Name cannot be null or empty");
    }

    @Test
    void shouldUpdateLastName() throws DomainException {
        Customer customer = new Customer(1L, "John", "Doe", "john@example.com");
        customer.pullEvents(true);
        customer.updateLastName("Smith");
        assertThat(customer.getLastName()).isEqualTo("Smith");
    }

    @Test
    void shouldEmitCustomerUpdatedEventOnLastNameChange() throws DomainException {
        Customer customer = new Customer(1L, "John", "Doe", "john@example.com");
        customer.pullEvents(true);
        customer.updateLastName("Smith");
        List<DomainEvent> events = customer.pullEvents(false);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(CustomerUpdatedEvent.class);
        assertThat(((CustomerUpdatedEvent) events.get(0)).getFieldName()).isEqualTo("lastName");
    }

    @Test
    void shouldRejectUpdateLastNameToEmpty() throws DomainException {
        Customer customer = new Customer(1L, "John", "Doe", "john@example.com");
        assertThatThrownBy(() -> customer.updateLastName(""))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Last name cannot be null or empty");
    }

    @Test
    void shouldDeleteAndEmitEvent() throws DomainException {
        Customer customer = new Customer(1L, "John", "Doe", "john@example.com");
        customer.pullEvents(true);
        customer.delete();
        List<DomainEvent> events = customer.pullEvents(false);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(CustomerDeletedEvent.class);
        CustomerDeletedEvent event = (CustomerDeletedEvent) events.get(0);
        assertThat(event.getCustomerId()).isEqualTo(1L);
    }

    @Test
    void shouldAcceptEmailWithSubdomain() throws DomainException {
        Customer customer = new Customer(1L, "John", "Doe", "john@sub.example.com");
        assertThat(customer.getEmail()).isEqualTo("john@sub.example.com");
    }

    @Test
    void shouldAcceptEmailWithPlusSign() throws DomainException {
        Customer customer = new Customer(1L, "John", "Doe", "john+tag@example.com");
        assertThat(customer.getEmail()).isEqualTo("john+tag@example.com");
    }

    @Test
    void shouldClearEventsOnPullWithClear() throws DomainException {
        Customer customer = new Customer(1L, "John", "Doe", "john@example.com");
        customer.pullEvents(true);
        assertThat(customer.pullEvents(false)).isEmpty();
    }
}
