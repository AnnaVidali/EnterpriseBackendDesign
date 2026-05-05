package com.application.enterprisebackenddesign.api.customer;

import com.application.enterprisebackenddesign.application.customer.*;
import com.application.enterprisebackenddesign.domain.customer.Customer;
import com.application.enterprisebackenddesign.domain.customer.CustomerRepository;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final DeleteCustomerUseCase deleteCustomerUseCase;
    private final GetCustomerUseCase getCustomerUseCase;
    private final ListCustomersUseCase listCustomersUseCase;
    private final UpdateCustomerUseCase updateCustomerUseCase;
    private final CustomerMapper customerMapper;

    public CustomerController(CreateCustomerUseCase createCustomerUseCase, DeleteCustomerUseCase deleteCustomerUseCase, GetCustomerUseCase getCustomerUseCase, ListCustomersUseCase listCustomersUseCase, UpdateCustomerUseCase updateCustomerUseCase, CustomerMapper customerMapper) {
        this.createCustomerUseCase = createCustomerUseCase;
        this.deleteCustomerUseCase = deleteCustomerUseCase;
        this.getCustomerUseCase = getCustomerUseCase;
        this.listCustomersUseCase = listCustomersUseCase;
        this.updateCustomerUseCase = updateCustomerUseCase;
        this.customerMapper = customerMapper;
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse createCustomer(@RequestBody CustomerRequest customerRequest) throws DomainException {
        Customer customer = createCustomerUseCase.create(UUID.randomUUID().getMostSignificantBits(), customerRequest);
        return customerMapper.toResponse(customer);
    }

    @GetMapping("/{id}")
    public CustomerResponse getCustomer(@PathVariable Long id) throws DomainException {
        Customer customer = getCustomerUseCase.getById(id);
        return customerMapper.toResponse(customer);
    }

    @GetMapping("/all")
    public List<CustomerResponse> getAllCustomers() throws DomainException {
        List<Customer> customers = listCustomersUseCase.listAll();
        return customers.stream()
                .map(customerMapper::toResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    @PutMapping("/{id}")
    public CustomerResponse updateCustomer(@PathVariable Long id, @RequestBody CustomerRequest customerRequest) throws DomainException {
        Customer customer = updateCustomerUseCase.update(id, customerRequest);
        return customerMapper.toResponse(customer);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable Long id) throws DomainException {
        deleteCustomerUseCase.delete(id);
    }
}
