package com.application.enterprisebackenddesign.domain.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findById(Long id);

    List<Customer> findAll();

    Page<Customer> findAll(Pageable pageable);

    void deleteById(Long customerId);
}
