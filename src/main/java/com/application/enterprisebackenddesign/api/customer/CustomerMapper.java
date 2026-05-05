package com.application.enterprisebackenddesign.api.customer;

import com.application.enterprisebackenddesign.domain.customer.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getLastName(),
                customer.getEmail());
    }
}
