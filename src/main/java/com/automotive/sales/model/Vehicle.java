package com.automotive.sales.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle extends BaseEntity {

    @Column(name = "vin", unique = true, nullable = false, length = 17)
    @NotBlank(message = "VIN is required")
    @Size(min = 17, max = 17, message = "VIN must be exactly 17 characters")
    private String vin;

    @Column(name = "make", nullable = false)
    @NotBlank(message = "Make is required")
    private String make;

    @Column(name = "model", nullable = false)
    @NotBlank(message = "Model is required")
    private String model;

    @Column(name = "year", nullable = false)
    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be after 1900")
    @Max(value = 2030, message = "Year cannot be in the future")
    private Integer year;

    @Column(name = "color")
    private String color;

    @Column(name = "engine_type")
    private String engineType;

    @Column(name = "transmission")
    private String transmission;

    @Column(name = "fuel_type")
    private String fuelType;

    @Column(name = "mileage")
    @Min(value = 0, message = "Mileage cannot be negative")
    private Integer mileage;

    @Column(name = "purchase_price", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Purchase price cannot be negative")
    private BigDecimal purchasePrice;

    @Column(name = "selling_price", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Selling price cannot be negative")
    private BigDecimal sellingPrice;

    @Column(name = "msrp", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "MSRP cannot be negative")
    private BigDecimal msrp;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false)
    @Builder.Default
    private VehicleCondition condition = VehicleCondition.NEW;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "location")
    private String location;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Sale> sales;

    public enum VehicleStatus {
        AVAILABLE, RESERVED, SOLD, MAINTENANCE, DISCONTINUED
    }

    public enum VehicleCondition {
        NEW, USED, CERTIFIED_PRE_OWNED, DAMAGED
    }

    public BigDecimal getPotentialProfit() {
        if (sellingPrice != null && purchasePrice != null) {
            return sellingPrice.subtract(purchasePrice);
        }
        return BigDecimal.ZERO;
    }

    public String getFullName() {
        return String.format("%d %s %s", year, make, model);
    }
}
