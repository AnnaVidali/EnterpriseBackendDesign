package com.application.enterprisebackenddesign.infrastructure.persistence.customer;

import com.application.enterprisebackenddesign.infrastructure.persistence.customer.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCustomerRepository extends JpaRepository<CustomerEntity, Long> {

    void deleteById(Long id);
}
