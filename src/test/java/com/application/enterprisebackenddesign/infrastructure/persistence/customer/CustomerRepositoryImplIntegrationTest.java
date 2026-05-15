package com.application.enterprisebackenddesign.infrastructure.persistence.customer;

import com.application.enterprisebackenddesign.TestcontainersConfiguration;
import com.application.enterprisebackenddesign.domain.customer.Customer;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Sql(statements = "DELETE FROM customers", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class CustomerRepositoryImplIntegrationTest {

    @Autowired
    private CustomerRepositoryImpl customerRepository;

    @Test
    void shouldSaveAndFindById() throws Exception {
        Customer customer = new Customer(1L, "John", "Doe", "john@example.com");
        Customer saved = customerRepository.save(customer);

        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getName()).isEqualTo("John");
        assertThat(saved.getLastName()).isEqualTo("Doe");
        assertThat(saved.getEmail()).isEqualTo("john@example.com");

        Optional<Customer> found = customerRepository.findById(1L);
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John");
        assertThat(found.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void shouldFindAll() throws Exception {
        customerRepository.save(new Customer(1L, "John", "Doe", "john@example.com"));
        customerRepository.save(new Customer(2L, "Jane", "Smith", "jane@example.com"));

        var customers = customerRepository.findAll();
        assertThat(customers).hasSize(2);
    }

    @Test
    void shouldFindAllWithPagination() throws Exception {
        customerRepository.save(new Customer(1L, "Alice", "A", "alice@example.com"));
        customerRepository.save(new Customer(2L, "Bob", "B", "bob@example.com"));
        customerRepository.save(new Customer(3L, "Charlie", "C", "charlie@example.com"));

        Page<Customer> page = customerRepository.findAll(PageRequest.of(0, 2));
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void shouldUpdate() throws Exception {
        customerRepository.save(new Customer(1L, "John", "Doe", "john@example.com"));

        Customer updated = new Customer(1L, "Johnny", "Doe", "johnny@example.com");
        customerRepository.save(updated);

        Optional<Customer> found = customerRepository.findById(1L);
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Johnny");
        assertThat(found.get().getEmail()).isEqualTo("johnny@example.com");
    }

    @Test
    void shouldDelete() throws Exception {
        customerRepository.save(new Customer(1L, "John", "Doe", "john@example.com"));
        customerRepository.deleteById(1L);

        Optional<Customer> found = customerRepository.findById(1L);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        Optional<Customer> found = customerRepository.findById(9999L);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldRejectInvalidCustomer() {
        assertThrows(DomainException.BusinessRuleViolationException.class,
                () -> new Customer(1L, "", "Doe", "john@example.com"));
        assertThrows(DomainException.BusinessRuleViolationException.class,
                () -> new Customer(1L, "John", "", "john@example.com"));
        assertThrows(DomainException.BusinessRuleViolationException.class,
                () -> new Customer(1L, "John", "Doe", "invalid-email"));
    }
}
