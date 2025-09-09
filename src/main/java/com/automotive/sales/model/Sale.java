package com.automotive.sales.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @NotNull(message = "Vehicle is required")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @NotNull(message = "Customer is required")
    private Customer customer;

    @Column(name = "sale_date", nullable = false)
    @NotNull(message = "Sale date is required")
    private LocalDate saleDate;

    @Column(name = "sale_price", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Sale price is required")
    @DecimalMin(value = "0.0", message = "Sale price cannot be negative")
    private BigDecimal salePrice;

    @Column(name = "down_payment", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Down payment cannot be negative")
    private BigDecimal downPayment;

    @Column(name = "trade_in_value", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Trade-in value cannot be negative")
    private BigDecimal tradeInValue;

    @Column(name = "financing_amount", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Financing amount cannot be negative")
    private BigDecimal financingAmount;

    @Column(name = "interest_rate", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "Interest rate cannot be negative")
    @DecimalMax(value = "100.0", message = "Interest rate cannot exceed 100%")
    private BigDecimal interestRate;

    @Column(name = "loan_term_months")
    @Min(value = 1, message = "Loan term must be at least 1 month")
    @Max(value = 120, message = "Loan term cannot exceed 120 months")
    private Integer loanTermMonths;

    @Column(name = "monthly_payment", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Monthly payment cannot be negative")
    private BigDecimal monthlyPayment;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.CASH;

    @Enumerated(EnumType.STRING)
    @Column(name = "sale_status", nullable = false)
    @Builder.Default
    private SaleStatus status = SaleStatus.PENDING;

    @Column(name = "salesperson_name")
    private String salespersonName;

    @Column(name = "salesperson_email")
    @Email(message = "Salesperson email should be valid")
    private String salespersonEmail;

    @Column(name = "commission_rate", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "Commission rate cannot be negative")
    @DecimalMax(value = "100.0", message = "Commission rate cannot exceed 100%")
    private BigDecimal commissionRate;

    @Column(name = "commission_amount", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Commission amount cannot be negative")
    private BigDecimal commissionAmount;

    @Column(name = "warranty_months")
    @Min(value = 0, message = "Warranty months cannot be negative")
    private Integer warrantyMonths;

    @Column(name = "extended_warranty")
    @Builder.Default
    private Boolean extendedWarranty = false;

    @Column(name = "extended_warranty_cost", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Extended warranty cost cannot be negative")
    private BigDecimal extendedWarrantyCost;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "contract_signed_at")
    private LocalDateTime contractSignedAt;

    @Column(name = "is_finalized")
    @Builder.Default
    private Boolean isFinalized = false;

    public enum PaymentMethod {
        CASH, FINANCING, LEASE, TRADE_IN, COMBINATION
    }

    public enum SaleStatus {
        PENDING, APPROVED, COMPLETED, CANCELLED, REFUNDED
    }

    public BigDecimal getTotalProfit() {
        BigDecimal profit = BigDecimal.ZERO;
        if (salePrice != null && vehicle != null && vehicle.getPurchasePrice() != null) {
            profit = salePrice.subtract(vehicle.getPurchasePrice());
        }
        
        // Add extended warranty profit
        if (extendedWarrantyCost != null) {
            profit = profit.add(extendedWarrantyCost);
        }
        
        // Subtract commission
        if (commissionAmount != null) {
            profit = profit.subtract(commissionAmount);
        }
        
        return profit;
    }

    public BigDecimal getNetAmount() {
        BigDecimal netAmount = salePrice != null ? salePrice : BigDecimal.ZERO;
        
        if (tradeInValue != null) {
            netAmount = netAmount.subtract(tradeInValue);
        }
        
        if (extendedWarrantyCost != null) {
            netAmount = netAmount.add(extendedWarrantyCost);
        }
        
        return netAmount;
    }

    public BigDecimal getRemainingBalance() {
        BigDecimal remaining = getNetAmount();
        
        if (downPayment != null) {
            remaining = remaining.subtract(downPayment);
        }
        
        return remaining.max(BigDecimal.ZERO);
    }

    public boolean isFullyPaid() {
        return paymentMethod == PaymentMethod.CASH || getRemainingBalance().compareTo(BigDecimal.ZERO) == 0;
    }
}
