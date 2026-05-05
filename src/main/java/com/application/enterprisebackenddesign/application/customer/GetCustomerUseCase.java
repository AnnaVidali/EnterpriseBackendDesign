package com.application.enterprisebackenddesign.application.customer;

import com.application.enterprisebackenddesign.domain.customer.Customer;
import com.application.enterprisebackenddesign.domain.customer.CustomerRepository;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class GetCustomerUseCase {

    private final CustomerRepository customerRepository;

    public GetCustomerUseCase(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;

    }

    public Customer getById(Long customerId) throws DomainException {
        return customerRepository.findById(customerId).orElseThrow(() -> new DomainException.ResourceNotFoundException("Customer not found."));
    }
}
