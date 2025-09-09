package com.automotive.sales.service;

import com.automotive.sales.repository.SaleRepository;
import com.automotive.sales.repository.VehicleRepository;
import com.automotive.sales.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnalyticsService {

    private final SaleRepository saleRepository;
    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;

    public RevenueAnalytics getRevenueAnalytics(LocalDate startDate, LocalDate endDate) {
        log.info("Generating revenue analytics for period: {} to {}", startDate, endDate);
        
        BigDecimal totalRevenue = saleRepository.getRevenueByDateRange(startDate, endDate);
        BigDecimal totalProfit = saleRepository.getProfitByDateRange(startDate, endDate);
        BigDecimal averageSalePrice = saleRepository.getAverageSalePrice();
        
        // Calculate profit margin
        BigDecimal profitMargin = BigDecimal.ZERO;
        if (totalRevenue != null && totalRevenue.compareTo(BigDecimal.ZERO) > 0 && totalProfit != null) {
            profitMargin = totalProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        
        List<Object[]> monthlySales = saleRepository.getMonthlySalesReport();
        List<MonthlySalesData> monthlyData = monthlySales.stream()
                .map(row -> MonthlySalesData.builder()
                        .year((Integer) row[0])
                        .month((Integer) row[1])
                        .salesCount((Long) row[2])
                        .revenue((BigDecimal) row[3])
                        .build())
                .collect(Collectors.toList());
        
        return RevenueAnalytics.builder()
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .totalProfit(totalProfit != null ? totalProfit : BigDecimal.ZERO)
                .profitMargin(profitMargin)
                .averageSalePrice(averageSalePrice != null ? averageSalePrice : BigDecimal.ZERO)
                .monthlySalesData(monthlyData)
                .build();
    }

    public SalesPerformanceAnalytics getSalesPerformanceAnalytics() {
        log.info("Generating sales performance analytics");
        
        List<Object[]> salespersonPerformance = saleRepository.getSalespersonPerformance();
        List<SalespersonPerformance> performanceData = salespersonPerformance.stream()
                .map(row -> SalespersonPerformance.builder()
                        .salespersonEmail((String) row[0])
                        .salesCount((Long) row[1])
                        .totalRevenue((BigDecimal) row[2])
                        .averageSaleValue(((BigDecimal) row[2]).divide(BigDecimal.valueOf((Long) row[1]), 2, RoundingMode.HALF_UP))
                        .build())
                .collect(Collectors.toList());
        
        List<Object[]> paymentMethodDistribution = saleRepository.getPaymentMethodDistribution();
        Map<String, Long> paymentMethods = paymentMethodDistribution.stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> (Long) row[1]
                ));
        
        return SalesPerformanceAnalytics.builder()
                .salespersonPerformance(performanceData)
                .paymentMethodDistribution(paymentMethods)
                .build();
    }

    public InventoryAnalytics getInventoryAnalytics() {
        log.info("Generating inventory analytics");
        
        Long availableCount = vehicleRepository.countByStatus(com.automotive.sales.model.Vehicle.VehicleStatus.AVAILABLE);
        Long soldCount = vehicleRepository.countByStatus(com.automotive.sales.model.Vehicle.VehicleStatus.SOLD);
        Long reservedCount = vehicleRepository.countByStatus(com.automotive.sales.model.Vehicle.VehicleStatus.RESERVED);
        Long maintenanceCount = vehicleRepository.countByStatus(com.automotive.sales.model.Vehicle.VehicleStatus.MAINTENANCE);
        
        BigDecimal averageSellingPrice = vehicleRepository.getAverageSellingPrice();
        BigDecimal totalPotentialProfit = vehicleRepository.getTotalPotentialProfit();
        
        List<Object[]> vehicleCountByMake = vehicleRepository.getVehicleCountByMake();
        Map<String, Long> makeDistribution = vehicleCountByMake.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
        
        // Calculate inventory turnover rate (simplified)
        BigDecimal inventoryTurnover = BigDecimal.ZERO;
        Long totalVehicles = availableCount + soldCount + reservedCount + maintenanceCount;
        if (totalVehicles > 0 && soldCount > 0) {
            inventoryTurnover = BigDecimal.valueOf(soldCount)
                    .divide(BigDecimal.valueOf(totalVehicles), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        
        return InventoryAnalytics.builder()
                .availableVehicles(availableCount)
                .soldVehicles(soldCount)
                .reservedVehicles(reservedCount)
                .maintenanceVehicles(maintenanceCount)
                .averageSellingPrice(averageSellingPrice != null ? averageSellingPrice : BigDecimal.ZERO)
                .totalPotentialProfit(totalPotentialProfit != null ? totalPotentialProfit : BigDecimal.ZERO)
                .inventoryTurnoverRate(inventoryTurnover)
                .vehiclesByMake(makeDistribution)
                .build();
    }

    public CustomerAnalytics getCustomerAnalytics() {
        log.info("Generating customer analytics");
        
        Long totalCustomers = customerRepository.count();
        Long activeCustomers = (long) customerRepository.findByIsActiveTrue().size();
        Long businessCustomers = customerRepository.countByCustomerType(com.automotive.sales.model.Customer.CustomerType.BUSINESS);
        Long individualCustomers = customerRepository.countByCustomerType(com.automotive.sales.model.Customer.CustomerType.INDIVIDUAL);
        
        Double averageCreditScore = customerRepository.getAverageCreditScore();
        
        List<Object[]> customersByState = customerRepository.getCustomerCountByState();
        Map<String, Long> stateDistribution = customersByState.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
        
        // Calculate customer retention rate (simplified - active vs total)
        BigDecimal retentionRate = BigDecimal.ZERO;
        if (totalCustomers > 0) {
            retentionRate = BigDecimal.valueOf(activeCustomers)
                    .divide(BigDecimal.valueOf(totalCustomers), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        
        return CustomerAnalytics.builder()
                .totalCustomers(totalCustomers)
                .activeCustomers(activeCustomers)
                .businessCustomers(businessCustomers)
                .individualCustomers(individualCustomers)
                .averageCreditScore(averageCreditScore != null ? averageCreditScore : 0.0)
                .customerRetentionRate(retentionRate)
                .customersByState(stateDistribution)
                .build();
    }

    public GrowthProjections getGrowthProjections(int monthsAhead) {
        log.info("Generating growth projections for {} months ahead", monthsAhead);
        
        // Get historical data for trend analysis
        List<Object[]> monthlySales = saleRepository.getMonthlySalesReport();
        
        // Simple linear projection based on recent trends
        List<ProjectedMonth> projections = new ArrayList<>();
        
        if (!monthlySales.isEmpty()) {
            // Calculate average monthly growth
            BigDecimal totalRevenue = BigDecimal.ZERO;
            int monthCount = 0;
            
            for (Object[] row : monthlySales) {
                totalRevenue = totalRevenue.add((BigDecimal) row[3]);
                monthCount++;
            }
            
            BigDecimal averageMonthlyRevenue = monthCount > 0 ? 
                    totalRevenue.divide(BigDecimal.valueOf(monthCount), 2, RoundingMode.HALF_UP) : 
                    BigDecimal.ZERO;
            
            // Project future months with 5% growth assumption
            BigDecimal growthRate = new BigDecimal("1.05");
            BigDecimal projectedRevenue = averageMonthlyRevenue;
            
            for (int i = 1; i <= monthsAhead; i++) {
                YearMonth futureMonth = YearMonth.now().plusMonths(i);
                projectedRevenue = projectedRevenue.multiply(growthRate);
                
                projections.add(ProjectedMonth.builder()
                        .year(futureMonth.getYear())
                        .month(futureMonth.getMonthValue())
                        .projectedRevenue(projectedRevenue)
                        .projectedSales((long) (projectedRevenue.divide(averageMonthlyRevenue.compareTo(BigDecimal.ZERO) > 0 ? 
                                saleRepository.getAverageSalePrice() : new BigDecimal("25000"), 0, RoundingMode.HALF_UP).longValue()))
                        .build());
            }
        }
        
        return GrowthProjections.builder()
                .projectedMonths(projections)
                .projectionBasis("Linear growth based on 12-month historical average with 5% monthly growth")
                .confidenceLevel(75) // Simplified confidence level
                .build();
    }

    // Data Transfer Objects
    @lombok.Data
    @lombok.Builder
    public static class RevenueAnalytics {
        private BigDecimal totalRevenue;
        private BigDecimal totalProfit;
        private BigDecimal profitMargin;
        private BigDecimal averageSalePrice;
        private List<MonthlySalesData> monthlySalesData;
    }

    @lombok.Data
    @lombok.Builder
    public static class MonthlySalesData {
        private Integer year;
        private Integer month;
        private Long salesCount;
        private BigDecimal revenue;
    }

    @lombok.Data
    @lombok.Builder
    public static class SalesPerformanceAnalytics {
        private List<SalespersonPerformance> salespersonPerformance;
        private Map<String, Long> paymentMethodDistribution;
    }

    @lombok.Data
    @lombok.Builder
    public static class SalespersonPerformance {
        private String salespersonEmail;
        private Long salesCount;
        private BigDecimal totalRevenue;
        private BigDecimal averageSaleValue;
    }

    @lombok.Data
    @lombok.Builder
    public static class InventoryAnalytics {
        private Long availableVehicles;
        private Long soldVehicles;
        private Long reservedVehicles;
        private Long maintenanceVehicles;
        private BigDecimal averageSellingPrice;
        private BigDecimal totalPotentialProfit;
        private BigDecimal inventoryTurnoverRate;
        private Map<String, Long> vehiclesByMake;
    }

    @lombok.Data
    @lombok.Builder
    public static class CustomerAnalytics {
        private Long totalCustomers;
        private Long activeCustomers;
        private Long businessCustomers;
        private Long individualCustomers;
        private Double averageCreditScore;
        private BigDecimal customerRetentionRate;
        private Map<String, Long> customersByState;
    }

    @lombok.Data
    @lombok.Builder
    public static class GrowthProjections {
        private List<ProjectedMonth> projectedMonths;
        private String projectionBasis;
        private Integer confidenceLevel;
    }

    @lombok.Data
    @lombok.Builder
    public static class ProjectedMonth {
        private Integer year;
        private Integer month;
        private BigDecimal projectedRevenue;
        private Long projectedSales;
    }
}
