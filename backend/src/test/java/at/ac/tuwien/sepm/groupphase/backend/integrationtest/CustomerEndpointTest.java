package at.ac.tuwien.sepm.groupphase.backend.integrationtest;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CustomerDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CustomerRegistrationDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.PaginationDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.AddressMapper;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.CustomerMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.Address;
import at.ac.tuwien.sepm.groupphase.backend.entity.Customer;
import at.ac.tuwien.sepm.groupphase.backend.repository.AddressRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.CustomerRepository;
import at.ac.tuwien.sepm.groupphase.backend.security.JwtTokenizer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CustomerEndpointTest implements TestData {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SecurityProperties securityProperties;
    @Autowired
    private CustomerMapper customerMapper;

    private final Address address = new Address(TEST_ADDRESS_STREET, TEST_ADDRESS_POSTALCODE, TEST_ADDRESS_HOUSENUMBER, 0, "0");
    private final Customer customer = new Customer(TEST_CUSTOMER_EMAIL, TEST_CUSTOMER_PASSWORD, TEST_CUSTOMER_NAME, TEST_CUSTOMER_LOGINNAME, address, 0L, "1");
    private final Customer customer2 = new Customer("mail@gmail.com", TEST_CUSTOMER_PASSWORD, TEST_CUSTOMER_NAME, "login", address, 0L, "");

    @BeforeEach
    @CacheEvict(value = "counts",allEntries = true)
    public void beforeEach() {
        customerRepository.deleteAll();
        Customer customer = new Customer(TEST_CUSTOMER_EMAIL, TEST_CUSTOMER_PASSWORD, TEST_CUSTOMER_NAME, TEST_CUSTOMER_LOGINNAME, address, 0L, "1");
    }

    @Test
    void givenNothing_whenPost_thenCustomerWithAllSetPropertiesPlusId() throws Exception {
        CustomerRegistrationDto customerDto = customerMapper.customerToCustomerRegistrationDto(customer);
        String body = objectMapper.writeValueAsString(customerDto);

        MvcResult mvcResult = this.mockMvc.perform(post(CUSTOMER_BASE_URI)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)
        )
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        CustomerRegistrationDto customerResponse = objectMapper.readValue(response.getContentAsString(),
            CustomerRegistrationDto.class);

        assertNotNull(customerResponse.getId());
        assertNotNull(customerResponse.getName());
        assertNotNull(customerResponse.getLoginName());
        assertNotNull(customerResponse.getEmail());
        assertNotNull(customerResponse.getAddress());
        customerResponse.setId(null);
        customerResponse.setPassword(null);
        customerResponse.getAddress().setId(null);
        customer.setPassword(null);

        assertAll(
            () -> assertEquals(customer.getEmail(), customerMapper.customerDtoToCustomer(customerResponse).getEmail()),
            () -> assertEquals(customer.getName(), customerMapper.customerDtoToCustomer(customerResponse).getName()),
            () -> assertEquals(customer.getLoginName(), customerMapper.customerDtoToCustomer(customerResponse).getLoginName()),
            () -> assertEquals(customer.getAddress(), customerMapper.customerDtoToCustomer(customerResponse).getAddress()),
            () -> assertEquals(customer.getPhoneNumber(), customerMapper.customerDtoToCustomer(customerResponse).getPhoneNumber())
        );
    }

    @Test
    void givenNothing_whenPostInvalid_then400() throws Exception {
        customer.setName(null);
        customer.setEmail(null);
        customer.setLoginName(null);
        CustomerRegistrationDto dto = customerMapper.customerToCustomerRegistrationDto(customer);
        String body = objectMapper.writeValueAsString(dto);

        MvcResult mvcResult = this.mockMvc.perform(post(CUSTOMER_BASE_URI)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertAll(
            () -> assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus()),
            () -> {
                String content = response.getContentAsString();
                content = content.substring(content.indexOf('[') + 1, content.indexOf(']'));
                String[] errors = content.split(",");
                assertEquals(5, errors.length);
            }
        );
    }

    @Test
    void givenNothing_whenAddressInvalid_then404() throws Exception {
        customer.setAddress(null);
        CustomerRegistrationDto dto = customerMapper.customerToCustomerRegistrationDto(customer);
        String body = objectMapper.writeValueAsString(dto);

        MvcResult mvcResult = this.mockMvc.perform(post(CUSTOMER_BASE_URI)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertAll(
            () -> assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus())
        );
    }

    @Test
    void givenNothing_whenFindAllCustomers_thenEmptyList() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(CUSTOMER_BASE_URI + "?page=0&pageCount=1")
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
        PaginationDto<CustomerDto> paginationDto = objectMapper.readValue(response.getContentAsString(),
            new TypeReference<>(){});

        assertEquals(0, paginationDto.getItems().size());
    }

    @Test
    void givenTwoCustomers_whenFindAllWithPage_thenListWithSizeOneCustomersWithAllPropertiesExceptPassword()
        throws Exception {
        CustomerRegistrationDto customerDto = customerMapper.customerToCustomerRegistrationDto(customer);
        String body = objectMapper.writeValueAsString(customerDto);
        CustomerRegistrationDto customerDto2 = customerMapper.customerToCustomerRegistrationDto(customer);
        String body2 = objectMapper.writeValueAsString(customerDto2);

        this.mockMvc.perform(post(CUSTOMER_BASE_URI)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        this.mockMvc.perform(post(CUSTOMER_BASE_URI)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body2)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();

        MvcResult mvcResultGet = this.mockMvc.perform(get(CUSTOMER_BASE_URI + "?page=0&page_count=10")
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse responseGet = mvcResultGet.getResponse();

        assertEquals(HttpStatus.OK.value(), responseGet.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseGet.getContentType());

        PaginationDto<CustomerDto> customerDtos = objectMapper.readValue(responseGet.getContentAsString(),
            new TypeReference<>() {
            });

        assertEquals(1, customerDtos.getItems().size());
        CustomerDto customerDtoCheck = customerDtos.getItems().get(0);
        Address addressCheck = addressMapper.addressDtoToAddress(customerDtoCheck.getAddress());
        assertAll(
            () -> assertEquals(TEST_CUSTOMER_NAME, customerDtoCheck.getName()),
            () -> assertEquals(TEST_CUSTOMER_LOGINNAME, customerDtoCheck.getLoginName()),
            () -> assertEquals(address.getStreet(), addressCheck.getStreet()),
            () -> assertEquals(address.getDoorNumber(), addressCheck.getDoorNumber()),
            () -> assertEquals(address.getHouseNumber(), addressCheck.getHouseNumber()),
            () -> assertEquals(address.getPostalCode(), addressCheck.getPostalCode()),
            () -> assertEquals(address.getStairNumber(), addressCheck.getStairNumber()),
            () -> assertEquals(TEST_CUSTOMER_EMAIL, customerDtoCheck.getEmail())
        );
    }

    @Test
    void givenTwoCustomers_whenGetCount_thenArrayWithTwo()
        throws Exception {

        CustomerRegistrationDto customerDto = customerMapper.customerToCustomerRegistrationDto(customer);
        String body = objectMapper.writeValueAsString(customerDto);
        CustomerRegistrationDto customerDto2 = customerMapper.customerToCustomerRegistrationDto(customer2);
        String body2 = objectMapper.writeValueAsString(customerDto2);
        MvcResult mvcResultGet1 = this.mockMvc.perform(post(CUSTOMER_BASE_URI)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MvcResult mvcResultGet2 = this.mockMvc.perform(post(CUSTOMER_BASE_URI)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body2)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse responseGet2 = mvcResultGet1.getResponse();
        MockHttpServletResponse responseGet1 = mvcResultGet2.getResponse();
        MvcResult mvcResultGet = this.mockMvc.perform(get(CUSTOMER_BASE_URI)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse responseGet = mvcResultGet.getResponse();
        assertEquals(HttpStatus.CREATED.value(), responseGet1.getStatus());
        assertEquals(HttpStatus.CREATED.value(), responseGet2.getStatus());

        assertEquals(HttpStatus.OK.value(), responseGet.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseGet.getContentType());

        PaginationDto<CustomerDto> customerDtos = objectMapper.readValue(responseGet.getContentAsString(),
            new TypeReference<>() {
            });
        assertEquals(2, customerDtos.getTotalItemCount());

    }

    @Test
    void givenTwoCustomers_whenGetCount_thenTwoInCache()
        throws Exception {

        CustomerRegistrationDto customerDto = customerMapper.customerToCustomerRegistrationDto(customer);
        String body = objectMapper.writeValueAsString(customerDto);
        CustomerRegistrationDto customerDto2 = customerMapper.customerToCustomerRegistrationDto(customer2);
        String body2 = objectMapper.writeValueAsString(customerDto2);
        MvcResult mvcResultGet1 = this.mockMvc.perform(post(CUSTOMER_BASE_URI)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MvcResult mvcResultGet2 = this.mockMvc.perform(post(CUSTOMER_BASE_URI)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body2)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse responseGet2 = mvcResultGet1.getResponse();
        MockHttpServletResponse responseGet1 = mvcResultGet2.getResponse();
        MvcResult mvcResultGet = this.mockMvc.perform(get(CUSTOMER_BASE_URI)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse responseGet = mvcResultGet.getResponse();
        assertEquals(HttpStatus.CREATED.value(), responseGet1.getStatus());
        assertEquals(HttpStatus.CREATED.value(), responseGet2.getStatus());

        assertEquals(HttpStatus.OK.value(), responseGet.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseGet.getContentType());

        PaginationDto<CustomerDto> customerDtos = objectMapper.readValue(responseGet.getContentAsString(),
            new TypeReference<>() {
            });
        assertEquals(2, cacheManager.getCache("counts").get("customers",Long.class));
    }

}
