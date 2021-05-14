package at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CustomerRegistrationDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface CustomerMapper {
    Customer customerDtoToCustomer(CustomerRegistrationDto dto);

    CustomerRegistrationDto customerToCustomerDto(Customer customer);

    List<CustomerRegistrationDto> customerListToCustomerDtoList(List<Customer> customers);
}
