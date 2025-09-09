package com.automotive.sales.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @Column(name = "first_name", nullable = false)
    @NotBlank(message = "First name is required")
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @NotBlank(message = "Last name is required")
    private String lastName;

    @Column(name = "email", unique = true, nullable = false)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @Column(name = "phone")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number should be valid")
    private String phone;

    @Column(name = "date_of_birth")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "country")
    private String country;

    @Column(name = "driver_license")
    private String driverLicense;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false)
    @Builder.Default
    private CustomerType customerType = CustomerType.INDIVIDUAL;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "tax_id")
    private String taxId;

    @Column(name = "credit_score")
    @Min(value = 300, message = "Credit score must be at least 300")
    @Max(value = 850, message = "Credit score cannot exceed 850")
    private Integer creditScore;

    @Column(name = "preferred_contact_method")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ContactMethod preferredContactMethod = ContactMethod.EMAIL;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Sale> sales;

    public enum CustomerType {
        INDIVIDUAL, BUSINESS, FLEET
    }

    public enum ContactMethod {
        EMAIL, PHONE, SMS, MAIL
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getDisplayName() {
        if (customerType == CustomerType.BUSINESS && companyName != null) {
            return companyName + " (" + getFullName() + ")";
        }
        return getFullName();
    }

    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        if (this.address != null) address.append(this.address);
        if (city != null) address.append(", ").append(city);
        if (state != null) address.append(", ").append(state);
        if (zipCode != null) address.append(" ").append(zipCode);
        if (country != null) address.append(", ").append(country);
        return address.toString();
    }
}
