package com.application.enterprisebackenddesign.infrastructure.persistence.customer;

import com.application.enterprisebackenddesign.domain.customer.Customer;
import com.application.enterprisebackenddesign.domain.customer.CustomerRepository;
import com.application.enterprisebackenddesign.infrastructure.persistence.customer.entity.CustomerEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email).map(customerMapper::toDomain);
    }

    @Override
    public List<Customer> findAll() {
        return customerRepository.findAll()
                .stream()
                .map(customerMapper::toDomain)
                .toList();
    }
}
