package com.automotive.sales.repository;

import com.automotive.sales.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    List<Customer> findByFirstNameAndLastName(String firstName, String lastName);

    List<Customer> findByCustomerType(Customer.CustomerType customerType);

    Page<Customer> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName, Pageable pageable);

    List<Customer> findByPhone(String phone);

    List<Customer> findByCity(String city);

    List<Customer> findByState(String state);

    List<Customer> findByIsActiveTrue();

    List<Customer> findByIsActiveFalse();

    @Query("SELECT c FROM Customer c WHERE c.dateOfBirth BETWEEN :startDate AND :endDate")
    List<Customer> findByDateOfBirthBetween(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    @Query("SELECT c FROM Customer c WHERE c.creditScore BETWEEN :minScore AND :maxScore")
    List<Customer> findByCreditScoreRange(@Param("minScore") Integer minScore,
                                         @Param("maxScore") Integer maxScore);

    @Query("SELECT c FROM Customer c WHERE c.creditScore >= :minScore")
    List<Customer> findByMinimumCreditScore(@Param("minScore") Integer minScore);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.customerType = :customerType")
    Long countByCustomerType(@Param("customerType") Customer.CustomerType customerType);

    @Query("SELECT AVG(c.creditScore) FROM Customer c WHERE c.creditScore IS NOT NULL")
    Double getAverageCreditScore();

    @Query("SELECT c FROM Customer c WHERE " +
           "(:firstName IS NULL OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
           "(:lastName IS NULL OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
           "(:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:customerType IS NULL OR c.customerType = :customerType) AND " +
           "(:city IS NULL OR LOWER(c.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:state IS NULL OR LOWER(c.state) LIKE LOWER(CONCAT('%', :state, '%'))) AND " +
           "(:isActive IS NULL OR c.isActive = :isActive)")
    Page<Customer> findCustomersWithFilters(@Param("firstName") String firstName,
                                           @Param("lastName") String lastName,
                                           @Param("email") String email,
                                           @Param("customerType") Customer.CustomerType customerType,
                                           @Param("city") String city,
                                           @Param("state") String state,
                                           @Param("isActive") Boolean isActive,
                                           Pageable pageable);

    @Query("SELECT c.state, COUNT(c) FROM Customer c WHERE c.state IS NOT NULL GROUP BY c.state ORDER BY COUNT(c) DESC")
    List<Object[]> getCustomerCountByState();

    @Query("SELECT c FROM Customer c WHERE SIZE(c.sales) > :minSales")
    List<Customer> findCustomersWithMinimumSales(@Param("minSales") int minSales);

    @Query("SELECT c FROM Customer c WHERE c.companyName IS NOT NULL AND c.customerType = 'BUSINESS'")
    List<Customer> findBusinessCustomers();
}
