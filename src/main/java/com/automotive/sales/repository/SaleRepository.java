package com.automotive.sales.repository;

import com.automotive.sales.model.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByStatus(Sale.SaleStatus status);

    List<Sale> findByCustomerId(Long customerId);

    List<Sale> findByVehicleId(Long vehicleId);

    List<Sale> findByPaymentMethod(Sale.PaymentMethod paymentMethod);

    List<Sale> findBySalespersonEmail(String salespersonEmail);

    @Query("SELECT s FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate")
    List<Sale> findBySaleDateBetween(@Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM Sale s WHERE s.salePrice BETWEEN :minPrice AND :maxPrice")
    List<Sale> findBySalePriceRange(@Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT s FROM Sale s WHERE s.deliveryDate BETWEEN :startDate AND :endDate")
    List<Sale> findByDeliveryDateBetween(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.status = :status")
    Long countByStatus(@Param("status") Sale.SaleStatus status);

    @Query("SELECT SUM(s.salePrice) FROM Sale s WHERE s.status = 'COMPLETED'")
    BigDecimal getTotalRevenue();

    @Query("SELECT SUM(s.salePrice) FROM Sale s WHERE s.status = 'COMPLETED' AND s.saleDate BETWEEN :startDate AND :endDate")
    BigDecimal getRevenueByDateRange(@Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    @Query("SELECT AVG(s.salePrice) FROM Sale s WHERE s.status = 'COMPLETED'")
    BigDecimal getAverageSalePrice();

    @Query("SELECT SUM(s.salePrice - s.vehicle.purchasePrice) FROM Sale s WHERE s.status = 'COMPLETED'")
    BigDecimal getTotalProfit();

    @Query("SELECT SUM(s.salePrice - s.vehicle.purchasePrice) FROM Sale s WHERE s.status = 'COMPLETED' AND s.saleDate BETWEEN :startDate AND :endDate")
    BigDecimal getProfitByDateRange(@Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM Sale s WHERE " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:paymentMethod IS NULL OR s.paymentMethod = :paymentMethod) AND " +
           "(:salespersonEmail IS NULL OR s.salespersonEmail = :salespersonEmail) AND " +
           "(:startDate IS NULL OR s.saleDate >= :startDate) AND " +
           "(:endDate IS NULL OR s.saleDate <= :endDate) AND " +
           "(:minPrice IS NULL OR s.salePrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR s.salePrice <= :maxPrice)")
    Page<Sale> findSalesWithFilters(@Param("status") Sale.SaleStatus status,
                                   @Param("paymentMethod") Sale.PaymentMethod paymentMethod,
                                   @Param("salespersonEmail") String salespersonEmail,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate,
                                   @Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice,
                                   Pageable pageable);

    @Query("SELECT s.salespersonEmail, COUNT(s), SUM(s.salePrice) FROM Sale s WHERE s.status = 'COMPLETED' GROUP BY s.salespersonEmail ORDER BY SUM(s.salePrice) DESC")
    List<Object[]> getSalespersonPerformance();

    @Query("SELECT YEAR(s.saleDate), MONTH(s.saleDate), COUNT(s), SUM(s.salePrice) FROM Sale s WHERE s.status = 'COMPLETED' GROUP BY YEAR(s.saleDate), MONTH(s.saleDate) ORDER BY YEAR(s.saleDate), MONTH(s.saleDate)")
    List<Object[]> getMonthlySalesReport();

    @Query("SELECT s.paymentMethod, COUNT(s) FROM Sale s GROUP BY s.paymentMethod ORDER BY COUNT(s) DESC")
    List<Object[]> getPaymentMethodDistribution();

    @Query("SELECT s FROM Sale s WHERE s.isFinalized = false AND s.status = 'PENDING'")
    List<Sale> findPendingUnfinalizedSales();

    @Query("SELECT s FROM Sale s WHERE s.extendedWarranty = true")
    List<Sale> findSalesWithExtendedWarranty();

    @Query("SELECT s FROM Sale s WHERE s.financingAmount > 0")
    List<Sale> findFinancedSales();

    @Query("SELECT s FROM Sale s WHERE s.tradeInValue > 0")
    List<Sale> findSalesWithTradeIn();

    @Query("SELECT AVG(s.commissionAmount) FROM Sale s WHERE s.commissionAmount IS NOT NULL AND s.status = 'COMPLETED'")
    BigDecimal getAverageCommission();

    @Query("SELECT s FROM Sale s WHERE s.deliveryDate IS NULL AND s.status = 'COMPLETED'")
    List<Sale> findCompletedSalesWithoutDelivery();
}
