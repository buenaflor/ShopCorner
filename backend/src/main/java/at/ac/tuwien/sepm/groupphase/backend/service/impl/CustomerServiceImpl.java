package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.entity.Address;
import at.ac.tuwien.sepm.groupphase.backend.entity.Customer;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.repository.AddressRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.CustomerRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.AddressService;
import at.ac.tuwien.sepm.groupphase.backend.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private final PasswordEncoder passwordEncoder;
    private final CustomerRepository customerRepository;
    private final AddressService addressService;
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public CustomerServiceImpl(PasswordEncoder passwordEncoder, CustomerRepository customerRepository, AddressRepository addressRepository, AddressService addressService) {
        this.passwordEncoder = passwordEncoder;
        this.customerRepository = customerRepository;
        this.addressService = addressService;
    }

    @Override
    public UserDetails loadUserByUsername(String loginName) {
        LOGGER.trace("loadUserByUsername({})", loginName);
        try {
            Customer customer = this.findCustomerByLoginName(loginName);
            List<GrantedAuthority> grantedAuthorities = AuthorityUtils.createAuthorityList("ROLE_CUSTOMER");
            return new User(customer.getLoginName(), customer.getPassword(), grantedAuthorities);
        } catch (NotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage(), e);
        }
    }

    @Override
    public Customer findCustomerByLoginName(String loginName) {
        LOGGER.trace("findCustomerByLoginName({})", loginName);
        Customer customer = customerRepository.findByLoginName(loginName);
        if (customer != null) {
            return customer;
        }
        throw new NotFoundException(String.format("Could not find the customer with the login name %s", loginName));
    }

    @Transactional
    @Override
    public Customer registerNewCustomer(Customer customer) {
        LOGGER.trace("registerNewCustomer({})", customer);
        Address address = addressService.addNewAddress(customer.getAddress());
        assignAddressToCustomer(customer, address.getId());
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        return customerRepository.save(customer);
    }

    @Override
    public List<Customer> getAllCustomers() {
        LOGGER.trace("getAllCustomers()");
        return customerRepository.findAll();
    }

    @Transactional
    public void assignAddressToCustomer(Customer customer, Long addressId) {
        LOGGER.trace("assignAddressToCustomer({},{})", customer, addressId);
        Address address = addressService.findAddressById(addressId);
        customer.setAddress(address);
    }

}
