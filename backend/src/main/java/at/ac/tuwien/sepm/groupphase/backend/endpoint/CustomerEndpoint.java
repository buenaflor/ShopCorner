package at.ac.tuwien.sepm.groupphase.backend.endpoint;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CustomerDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CustomerRegistrationDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.CustomerMapper;
import at.ac.tuwien.sepm.groupphase.backend.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping("api/v1/customers")
public class CustomerEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final CustomerMapper customerMapper;
    private final CustomerService customerService;

    @Autowired
    public CustomerEndpoint(CustomerMapper customerMapper, CustomerService customerService) {
        this.customerMapper = customerMapper;
        this.customerService = customerService;
    }

    /**
     * Adds a new customer to the database.
     *
     * @param dto The customer dto containing the customer information
     * @return The response dto containing the added customer
     */
    @PermitAll
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new customer account")
    public CustomerRegistrationDto registerNewCustomer(@Valid @RequestBody CustomerRegistrationDto dto) {
        LOGGER.info("POST api/v1/customers");
        CustomerRegistrationDto customer = customerMapper.customerToCustomerDto(customerService.registerNewCustomer(customerMapper.customerDtoToCustomer(dto)));
        customer.setPassword(null);
        return customer;
    }

    /**
     * Retrieves a page of customers from the database.
     *
     * @return A list of all the retrieved customers
     */
    @Secured({"ROLE_ADMIN", "ROLE_EMPLOYEE"})
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieve all customers", security = @SecurityRequirement(name = "apiKey"))
    public List<CustomerDto> getAllCustomers(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                             @RequestParam(name = "page_count", defaultValue = "15") Integer pageCount) {
        LOGGER.info("GET api/v1/customers?page={}&page_count={}", page, pageCount);
        return customerMapper.customerListToCustomerDtoList(customerService.getAllCustomers(page, pageCount).getContent());
    }

    /**
     * Retrieves the amount of customers in the database.
     *
     * @return The amount of customers in the database
     */
    @Secured({"ROLE_ADMIN", "ROLE_EMPLOYEE"})
    @GetMapping("/count")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieve count of customers", security = @SecurityRequirement(name = "apiKey"))
    public long getCustomerCount() {
        LOGGER.info("GET api/v1/customers");
        return customerService.getCustomerCount();
    }
}
