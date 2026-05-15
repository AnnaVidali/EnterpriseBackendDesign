package com.application.enterprisebackenddesign.api.customer;

import com.application.enterprisebackenddesign.api.shared.PageResponse;
import com.application.enterprisebackenddesign.application.customer.*;
import com.application.enterprisebackenddesign.domain.customer.Customer;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "CRUD operations for customer management")
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new customer",
            description = "Creates a customer with name, last name, and email. Generates a unique ID internally.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer created",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    public CustomerResponse createCustomer(@RequestBody CustomerRequest customerRequest) throws DomainException {
        Customer customer = createCustomerUseCase.create(UUID.randomUUID().getMostSignificantBits(), customerRequest);
        return customerMapper.toResponse(customer);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a customer by ID",
            description = "Returns the full customer profile for the given ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer found",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public CustomerResponse getCustomer(@PathVariable Long id) throws DomainException {
        Customer customer = getCustomerUseCase.getById(id);
        return customerMapper.toResponse(customer);
    }

    @GetMapping
    @Operation(summary = "List all customers",
            description = "Returns a paginated list of all registered customers.")
    @ApiResponse(responseCode = "200", description = "Paginated list of customers",
            content = @Content(schema = @Schema(implementation = PageResponse.class)))
    public PageResponse<CustomerResponse> getAllCustomers(
            @Parameter(description = "Pagination and sorting") @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        var customersPage = listCustomersUseCase.listAll(pageable);
        return PageResponse.from(customersPage, customersPage.map(customerMapper::toResponse).getContent());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a customer",
            description = "Updates the name, last name, and email of an existing customer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer updated",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    public CustomerResponse updateCustomer(@PathVariable Long id, @RequestBody CustomerRequest customerRequest) throws DomainException {
        Customer customer = updateCustomerUseCase.update(id, customerRequest);
        return customerMapper.toResponse(customer);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a customer",
            description = "Permanently removes a customer from the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Customer deleted"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public void deleteCustomer(@PathVariable Long id) throws DomainException {
        deleteCustomerUseCase.delete(id);
    }
}
