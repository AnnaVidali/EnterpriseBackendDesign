package com.application.enterprisebackenddesign.infrastructure.persistence.customer;

import com.application.enterprisebackenddesign.domain.customer.Customer;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.infrastructure.persistence.customer.entity.CustomerEntity;
import org.springframework.stereotype.Component;

/**
 * Maps between the Customer domain object and CustomerEntity JPA entity.
 *
 * Interview context: This mapper is the bridge between two worlds:
 * the rich domain model (with business rules, events, validated constructors)
 * and the JPA persistence model (with @Version, @Column, lazy loading).
 *
 * The @Component("infraCustomerMapper") qualifier prevents bean name conflicts
 * with any other CustomerMapper in the API layer (which maps domain → response DTO).
 * This is a common naming pattern when using hexagonal architecture with mappers
 * at multiple layers.
 *
 * toDomain(): The catch block wrapping DomainException is important — if database
 * data violates domain rules (e.g., someone directly edited the DB), we throw
 * an IllegalStateException rather than silently returning an invalid domain object.
 * This is a safety net for data integrity.
 */
@Component("infraCustomerMapper")
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
