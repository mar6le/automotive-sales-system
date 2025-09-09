package com.automotive.sales.service;

import com.automotive.sales.model.Customer;
import com.automotive.sales.model.Sale;
import com.automotive.sales.model.Vehicle;
import com.automotive.sales.repository.CustomerRepository;
import com.automotive.sales.repository.SaleRepository;
import com.automotive.sales.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SaleService {

    private final SaleRepository saleRepository;
    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;
    private final VehicleService vehicleService;

    public Sale createSale(Sale sale) {
        log.info("Creating new sale for vehicle ID: {} and customer ID: {}", 
                sale.getVehicle().getId(), sale.getCustomer().getId());
        
        // Validate vehicle exists and is available
        Vehicle vehicle = vehicleRepository.findById(sale.getVehicle().getId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + sale.getVehicle().getId()));
        
        if (vehicle.getStatus() != Vehicle.VehicleStatus.AVAILABLE && 
            vehicle.getStatus() != Vehicle.VehicleStatus.RESERVED) {
            throw new IllegalStateException("Vehicle is not available for sale. Current status: " + vehicle.getStatus());
        }
        
        // Validate customer exists and is active
        Customer customer = customerRepository.findById(sale.getCustomer().getId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + sale.getCustomer().getId()));
        
        if (!customer.getIsActive()) {
            throw new IllegalStateException("Customer is not active");
        }
        
        // Set default values if not provided
        if (sale.getSaleDate() == null) {
            sale.setSaleDate(LocalDate.now());
        }
        
        if (sale.getStatus() == null) {
            sale.setStatus(Sale.SaleStatus.PENDING);
        }
        
        if (sale.getPaymentMethod() == null) {
            sale.setPaymentMethod(Sale.PaymentMethod.CASH);
        }
        
        if (sale.getIsFinalized() == null) {
            sale.setIsFinalized(false);
        }
        
        // Calculate commission if rate is provided
        if (sale.getCommissionRate() != null && sale.getSalePrice() != null) {
            BigDecimal commission = sale.getSalePrice()
                    .multiply(sale.getCommissionRate())
                    .divide(BigDecimal.valueOf(100));
            sale.setCommissionAmount(commission);
        }
        
        // Reserve the vehicle
        vehicleService.reserveVehicle(vehicle.getId());
        
        Sale savedSale = saleRepository.save(sale);
        log.info("Sale created successfully with ID: {}", savedSale.getId());
        return savedSale;
    }

    @Transactional(readOnly = true)
    public Optional<Sale> getSaleById(Long id) {
        log.debug("Fetching sale with ID: {}", id);
        return saleRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Sale> getAllSales() {
        log.debug("Fetching all sales");
        return saleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Sale> getSalesByStatus(Sale.SaleStatus status) {
        log.debug("Fetching sales by status: {}", status);
        return saleRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Sale> getSalesByCustomer(Long customerId) {
        log.debug("Fetching sales for customer ID: {}", customerId);
        return saleRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public List<Sale> getSalesByVehicle(Long vehicleId) {
        log.debug("Fetching sales for vehicle ID: {}", vehicleId);
        return saleRepository.findByVehicleId(vehicleId);
    }

    @Transactional(readOnly = true)
    public Page<Sale> getSalesWithFilters(Sale.SaleStatus status, Sale.PaymentMethod paymentMethod,
                                         String salespersonEmail, LocalDate startDate, LocalDate endDate,
                                         BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        log.debug("Fetching sales with filters - status: {}, paymentMethod: {}, salesperson: {}", 
                 status, paymentMethod, salespersonEmail);
        return saleRepository.findSalesWithFilters(status, paymentMethod, salespersonEmail, 
                                                  startDate, endDate, minPrice, maxPrice, pageable);
    }

    public Sale updateSale(Long id, Sale saleDetails) {
        log.info("Updating sale with ID: {}", id);
        
        Sale existingSale = saleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found with ID: " + id));
        
        if (existingSale.getIsFinalized()) {
            throw new IllegalStateException("Cannot update finalized sale");
        }
        
        // Update fields
        existingSale.setSaleDate(saleDetails.getSaleDate());
        existingSale.setSalePrice(saleDetails.getSalePrice());
        existingSale.setDownPayment(saleDetails.getDownPayment());
        existingSale.setTradeInValue(saleDetails.getTradeInValue());
        existingSale.setFinancingAmount(saleDetails.getFinancingAmount());
        existingSale.setInterestRate(saleDetails.getInterestRate());
        existingSale.setLoanTermMonths(saleDetails.getLoanTermMonths());
        existingSale.setMonthlyPayment(saleDetails.getMonthlyPayment());
        existingSale.setPaymentMethod(saleDetails.getPaymentMethod());
        existingSale.setSalespersonName(saleDetails.getSalespersonName());
        existingSale.setSalespersonEmail(saleDetails.getSalespersonEmail());
        existingSale.setCommissionRate(saleDetails.getCommissionRate());
        existingSale.setWarrantyMonths(saleDetails.getWarrantyMonths());
        existingSale.setExtendedWarranty(saleDetails.getExtendedWarranty());
        existingSale.setExtendedWarrantyCost(saleDetails.getExtendedWarrantyCost());
        existingSale.setDeliveryDate(saleDetails.getDeliveryDate());
        existingSale.setDeliveryAddress(saleDetails.getDeliveryAddress());
        existingSale.setNotes(saleDetails.getNotes());
        
        // Recalculate commission if rate changed
        if (saleDetails.getCommissionRate() != null && saleDetails.getSalePrice() != null) {
            BigDecimal commission = saleDetails.getSalePrice()
                    .multiply(saleDetails.getCommissionRate())
                    .divide(BigDecimal.valueOf(100));
            existingSale.setCommissionAmount(commission);
        }
        
        Sale updatedSale = saleRepository.save(existingSale);
        log.info("Sale updated successfully with ID: {}", updatedSale.getId());
        return updatedSale;
    }

    public Sale approveSale(Long id) {
        log.info("Approving sale with ID: {}", id);
        
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found with ID: " + id));
        
        if (sale.getStatus() != Sale.SaleStatus.PENDING) {
            throw new IllegalStateException("Only pending sales can be approved");
        }
        
        sale.setStatus(Sale.SaleStatus.APPROVED);
        Sale updatedSale = saleRepository.save(sale);
        log.info("Sale approved successfully");
        return updatedSale;
    }

    public Sale completeSale(Long id) {
        log.info("Completing sale with ID: {}", id);
        
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found with ID: " + id));
        
        if (sale.getStatus() != Sale.SaleStatus.APPROVED) {
            throw new IllegalStateException("Only approved sales can be completed");
        }
        
        sale.setStatus(Sale.SaleStatus.COMPLETED);
        sale.setIsFinalized(true);
        sale.setContractSignedAt(LocalDateTime.now());
        
        // Mark vehicle as sold
        vehicleService.markVehicleAsSold(sale.getVehicle().getId());
        
        Sale updatedSale = saleRepository.save(sale);
        log.info("Sale completed successfully");
        return updatedSale;
    }

    public Sale cancelSale(Long id, String reason) {
        log.info("Cancelling sale with ID: {} with reason: {}", id, reason);
        
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found with ID: " + id));
        
        if (sale.getStatus() == Sale.SaleStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed sale");
        }
        
        sale.setStatus(Sale.SaleStatus.CANCELLED);
        sale.setNotes(sale.getNotes() != null ? sale.getNotes() + "\nCancellation reason: " + reason : "Cancellation reason: " + reason);
        
        // Make vehicle available again
        vehicleService.makeVehicleAvailable(sale.getVehicle().getId());
        
        Sale updatedSale = saleRepository.save(sale);
        log.info("Sale cancelled successfully");
        return updatedSale;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue() {
        log.debug("Calculating total revenue");
        return saleRepository.getTotalRevenue();
    }

    @Transactional(readOnly = true)
    public BigDecimal getRevenueByDateRange(LocalDate startDate, LocalDate endDate) {
        log.debug("Calculating revenue for date range: {} to {}", startDate, endDate);
        return saleRepository.getRevenueByDateRange(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalProfit() {
        log.debug("Calculating total profit");
        return saleRepository.getTotalProfit();
    }

    @Transactional(readOnly = true)
    public BigDecimal getProfitByDateRange(LocalDate startDate, LocalDate endDate) {
        log.debug("Calculating profit for date range: {} to {}", startDate, endDate);
        return saleRepository.getProfitByDateRange(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public BigDecimal getAverageSalePrice() {
        log.debug("Calculating average sale price");
        return saleRepository.getAverageSalePrice();
    }

    @Transactional(readOnly = true)
    public List<Object[]> getSalespersonPerformance() {
        log.debug("Getting salesperson performance data");
        return saleRepository.getSalespersonPerformance();
    }

    @Transactional(readOnly = true)
    public List<Object[]> getMonthlySalesReport() {
        log.debug("Getting monthly sales report");
        return saleRepository.getMonthlySalesReport();
    }

    @Transactional(readOnly = true)
    public List<Object[]> getPaymentMethodDistribution() {
        log.debug("Getting payment method distribution");
        return saleRepository.getPaymentMethodDistribution();
    }

    @Transactional(readOnly = true)
    public List<Sale> getPendingUnfinalizedSales() {
        log.debug("Getting pending unfinalized sales");
        return saleRepository.findPendingUnfinalizedSales();
    }

    @Transactional(readOnly = true)
    public Long getSaleCountByStatus(Sale.SaleStatus status) {
        log.debug("Getting sale count for status: {}", status);
        return saleRepository.countByStatus(status);
    }
}
