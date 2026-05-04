package com.application.enterprisebackenddesign.infrastructure.persistence.customer;

import com.application.enterprisebackenddesign.domain.customer.Customer;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.infrastructure.persistence.customer.entity.CustomerEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class CustomerMapper {

    public CustomerEntity toEntity(Customer customer) {
        CustomerEntity entity = new CustomerEntity();
        entity.setId(customer.getId());
        entity.setName(customer.getName());
        entity.setLastName(customer.getLastName());
        entity.setEmail(customer.getEmail());
        return entity;
    }

    public Customer toDomain(CustomerEntity customerEntity) {
        try {
            return new Customer(customerEntity.getId(), customerEntity.getName(), customerEntity.getLastName(), customerEntity.getEmail());
        } catch (DomainException.BusinessRuleViolationException e) {
            throw new IllegalStateException("Corrupted customer data in database", e);
        }
    }
}
