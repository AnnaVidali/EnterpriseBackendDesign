package com.application.enterprisebackenddesign.application.customer;

import com.application.enterprisebackenddesign.application.shared.DomainEventPublisher;
import com.application.enterprisebackenddesign.domain.customer.Customer;
import com.application.enterprisebackenddesign.domain.customer.CustomerRepository;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class DeleteCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final DomainEventPublisher eventPublisher;

    public DeleteCustomerUseCase(CustomerRepository customerRepository, DomainEventPublisher eventPublisher) {
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
    }

    public void delete(Long id) throws DomainException {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new DomainException.ResourceNotFoundException("Customer not found."));

        customer.delete();
        customer.pullEvents(true).forEach(eventPublisher::publish);

        customerRepository.deleteById(id);
    }
}
