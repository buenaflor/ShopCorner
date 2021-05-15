package at.ac.tuwien.sepm.groupphase.backend.endpoint;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CustomerRegistrationDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.CustomerMapper;
import at.ac.tuwien.sepm.groupphase.backend.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping("api/v1/users")
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
     * @param dto       The customer dto containing the customer information
     * @param addressId The id of the customers address
     * @return The response dto containing the added customer
     */
    @PermitAll
    @PostMapping("/{addressId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new customer account")
    public CustomerRegistrationDto registerNewCustomer(@Valid @RequestBody CustomerRegistrationDto dto, @PathVariable String addressId) {
        LOGGER.info("POST api/v1/address");
        return customerMapper.customerToCustomerDto(customerService.registerNewCustomer(customerMapper.customerDtoToCustomer(dto), Long.valueOf(addressId)));
    }

    /**
     * Retrieves all customers from the database.
     *
     * @return A list of all the retrieved customers
     */
    //TODO Change to Secured(ROLE_ADMIN)
    @PermitAll
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "retrieve all customers")
    public List<CustomerRegistrationDto> getAllCustomers() {
        LOGGER.info("GET api/v1//users");
        return customerMapper.customerListToCustomerDtoList(customerService.getAllCustomers());
    }
}
