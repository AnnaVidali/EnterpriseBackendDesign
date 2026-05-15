package com.application.enterprisebackenddesign.infrastructure.persistence.customer;

import com.application.enterprisebackenddesign.domain.customer.Customer;
import com.application.enterprisebackenddesign.domain.customer.CustomerRepository;
import com.application.enterprisebackenddesign.infrastructure.persistence.customer.entity.CustomerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Adapter implementation of the CustomerRepository port (Hexagonal Architecture).
 *
 * Interview context: This class is the "adapter" in Ports & Adapters terminology.
 * The domain defines the CustomerRepository INTERFACE (the port), and this class
 * IMPLEMENTS it using JPA. The domain layer never imports anything from
 * infrastructure — it only knows about the interface.
 *
 * Key pattern: Double mapping.
 * 1. Domain → Entity: CustomerMapper.toEntity() converts the rich domain object
 *    to a JPA-friendly entity with @Version, audit fields, etc.
 * 2. JPA save/load: Spring Data JPA handles the actual SQL.
 * 3. Entity → Domain: CustomerMapper.toDomain() converts back, re-running
 *    domain validation so corrupted database data is caught early.
 *
 * Why not just annotate the domain object with JPA annotations? Because that
 * couples the domain to the persistence framework. If we switch to NoSQL,
 * the domain model doesn't change — only the mapper and repository impl do.
 * This is the "persistence ignorance" principle from DDD.
 */
@Repository
public class CustomerRepositoryImpl implements CustomerRepository {

    private final SpringDataCustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerRepositoryImpl(SpringDataCustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Override
    public Customer save(Customer customer) {
        CustomerEntity entity = customerMapper.toEntity(customer);
        CustomerEntity saved = customerRepository.save(entity);
        return customerMapper.toDomain(saved);
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id).map(customerMapper::toDomain);
    }

    @Override
    public List<Customer> findAll() {
        return customerRepository.findAll()
                .stream()
                .map(customerMapper::toDomain)
                .toList();
    }

    @Override
    public Page<Customer> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable).map(customerMapper::toDomain);
    }

    @Override
    public void deleteById(Long customerId) {
        customerRepository.deleteById(customerId);
    }
}