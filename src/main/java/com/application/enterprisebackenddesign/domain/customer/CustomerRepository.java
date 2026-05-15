package com.application.enterprisebackenddesign.domain.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for the Customer aggregate.
 *
 * Hexagonal Architecture: This interface defines the contract between the
 * domain layer (which owns it) and the infrastructure layer (which implements it).
 * The domain uses CustomerRepository to persist and retrieve Customer aggregates
 * without knowing anything about databases, JPA, or SQL.
 *
 * This is the "port" — the implementation (CustomerRepositoryImpl) is the "adapter".
 * The interface lives in the domain package because it expresses a domain concept:
 * "customers can be saved and retrieved." The implementation lives in
 * infrastructure/persistence because it's a technical concern.
 */
public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findById(Long id);

    List<Customer> findAll();

    Page<Customer> findAll(Pageable pageable);

    void deleteById(Long customerId);
}
