package com.automotive.sales.service;

import com.automotive.sales.model.Customer;
import com.automotive.sales.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public Customer createCustomer(Customer customer) {
        log.info("Creating new customer with email: {}", customer.getEmail());
        
        // Check if email already exists
        if (customerRepository.findByEmail(customer.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Customer with email " + customer.getEmail() + " already exists");
        }
        
        // Set default values if not provided
        if (customer.getCustomerType() == null) {
            customer.setCustomerType(Customer.CustomerType.INDIVIDUAL);
        }
        
        if (customer.getPreferredContactMethod() == null) {
            customer.setPreferredContactMethod(Customer.ContactMethod.EMAIL);
        }
        
        if (customer.getIsActive() == null) {
            customer.setIsActive(true);
        }
        
        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer created successfully with ID: {}", savedCustomer.getId());
        return savedCustomer;
    }

    @Transactional(readOnly = true)
    public Optional<Customer> getCustomerById(Long id) {
        log.debug("Fetching customer with ID: {}", id);
        return customerRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Customer> getCustomerByEmail(String email) {
        log.debug("Fetching customer with email: {}", email);
        return customerRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        log.debug("Fetching all customers");
        return customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Customer> getActiveCustomers() {
        log.debug("Fetching active customers");
        return customerRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public Page<Customer> getCustomersWithFilters(String firstName, String lastName, String email,
                                                 Customer.CustomerType customerType, String city,
                                                 String state, Boolean isActive, Pageable pageable) {
        log.debug("Fetching customers with filters - firstName: {}, lastName: {}, email: {}, type: {}", 
                 firstName, lastName, email, customerType);
        return customerRepository.findCustomersWithFilters(firstName, lastName, email, customerType, 
                                                          city, state, isActive, pageable);
    }

    @Transactional(readOnly = true)
    public List<Customer> getCustomersByType(Customer.CustomerType customerType) {
        log.debug("Fetching customers by type: {}", customerType);
        return customerRepository.findByCustomerType(customerType);
    }

    @Transactional(readOnly = true)
    public List<Customer> getBusinessCustomers() {
        log.debug("Fetching business customers");
        return customerRepository.findBusinessCustomers();
    }

    @Transactional(readOnly = true)
    public List<Customer> getCustomersByCreditScoreRange(Integer minScore, Integer maxScore) {
        log.debug("Fetching customers with credit score range: {} - {}", minScore, maxScore);
        return customerRepository.findByCreditScoreRange(minScore, maxScore);
    }

    @Transactional(readOnly = true)
    public List<Customer> getHighCreditScoreCustomers(Integer minScore) {
        log.debug("Fetching customers with minimum credit score: {}", minScore);
        return customerRepository.findByMinimumCreditScore(minScore);
    }

    public Customer updateCustomer(Long id, Customer customerDetails) {
        log.info("Updating customer with ID: {}", id);
        
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + id));

        // Check if email is being changed and if new email already exists
        if (!existingCustomer.getEmail().equals(customerDetails.getEmail())) {
            if (customerRepository.findByEmail(customerDetails.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Customer with email " + customerDetails.getEmail() + " already exists");
            }
        }

        // Update fields
        existingCustomer.setFirstName(customerDetails.getFirstName());
        existingCustomer.setLastName(customerDetails.getLastName());
        existingCustomer.setEmail(customerDetails.getEmail());
        existingCustomer.setPhone(customerDetails.getPhone());
        existingCustomer.setDateOfBirth(customerDetails.getDateOfBirth());
        existingCustomer.setAddress(customerDetails.getAddress());
        existingCustomer.setCity(customerDetails.getCity());
        existingCustomer.setState(customerDetails.getState());
        existingCustomer.setZipCode(customerDetails.getZipCode());
        existingCustomer.setCountry(customerDetails.getCountry());
        existingCustomer.setDriverLicense(customerDetails.getDriverLicense());
        existingCustomer.setCustomerType(customerDetails.getCustomerType());
        existingCustomer.setCompanyName(customerDetails.getCompanyName());
        existingCustomer.setTaxId(customerDetails.getTaxId());
        existingCustomer.setCreditScore(customerDetails.getCreditScore());
        existingCustomer.setPreferredContactMethod(customerDetails.getPreferredContactMethod());
        existingCustomer.setNotes(customerDetails.getNotes());
        existingCustomer.setIsActive(customerDetails.getIsActive());

        Customer updatedCustomer = customerRepository.save(existingCustomer);
        log.info("Customer updated successfully with ID: {}", updatedCustomer.getId());
        return updatedCustomer;
    }

    public Customer deactivateCustomer(Long id) {
        log.info("Deactivating customer with ID: {}", id);
        
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + id));
        
        customer.setIsActive(false);
        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Customer deactivated successfully");
        return updatedCustomer;
    }

    public Customer activateCustomer(Long id) {
        log.info("Activating customer with ID: {}", id);
        
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + id));
        
        customer.setIsActive(true);
        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Customer activated successfully");
        return updatedCustomer;
    }

    public void deleteCustomer(Long id) {
        log.info("Deleting customer with ID: {}", id);
        
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + id));
        
        // Check if customer has any sales
        if (customer.getSales() != null && !customer.getSales().isEmpty()) {
            throw new IllegalStateException("Cannot delete customer with existing sales records");
        }
        
        customerRepository.delete(customer);
        log.info("Customer deleted successfully");
    }

    @Transactional(readOnly = true)
    public Long getCustomerCountByType(Customer.CustomerType customerType) {
        log.debug("Getting customer count for type: {}", customerType);
        return customerRepository.countByCustomerType(customerType);
    }

    @Transactional(readOnly = true)
    public Double getAverageCreditScore() {
        log.debug("Calculating average credit score");
        return customerRepository.getAverageCreditScore();
    }

    @Transactional(readOnly = true)
    public List<Object[]> getCustomerCountByState() {
        log.debug("Getting customer count by state");
        return customerRepository.getCustomerCountByState();
    }

    @Transactional(readOnly = true)
    public List<Customer> getCustomersWithMinimumSales(int minSales) {
        log.debug("Getting customers with minimum sales: {}", minSales);
        return customerRepository.findCustomersWithMinimumSales(minSales);
    }

    @Transactional(readOnly = true)
    public Page<Customer> searchCustomers(String searchTerm, Pageable pageable) {
        log.debug("Searching customers with term: {}", searchTerm);
        return customerRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                searchTerm, searchTerm, pageable);
    }

    public Customer updateCreditScore(Long id, Integer creditScore) {
        log.info("Updating credit score for customer ID: {} to {}", id, creditScore);
        
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + id));
        
        if (creditScore < 300 || creditScore > 850) {
            throw new IllegalArgumentException("Credit score must be between 300 and 850");
        }
        
        customer.setCreditScore(creditScore);
        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Credit score updated successfully");
        return updatedCustomer;
    }
}
