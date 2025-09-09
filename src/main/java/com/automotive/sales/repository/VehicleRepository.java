package com.automotive.sales.repository;

import com.automotive.sales.model.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByVin(String vin);

    List<Vehicle> findByStatus(Vehicle.VehicleStatus status);

    List<Vehicle> findByMakeAndModel(String make, String model);

    List<Vehicle> findByYear(Integer year);

    List<Vehicle> findByYearBetween(Integer startYear, Integer endYear);

    Page<Vehicle> findByMakeContainingIgnoreCase(String make, Pageable pageable);

    Page<Vehicle> findByModelContainingIgnoreCase(String model, Pageable pageable);

    @Query("SELECT v FROM Vehicle v WHERE v.sellingPrice BETWEEN :minPrice AND :maxPrice")
    List<Vehicle> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                   @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT v FROM Vehicle v WHERE v.status = :status AND v.sellingPrice <= :maxPrice")
    List<Vehicle> findAvailableVehiclesUnderPrice(@Param("status") Vehicle.VehicleStatus status,
                                                  @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT v FROM Vehicle v WHERE v.purchaseDate BETWEEN :startDate AND :endDate")
    List<Vehicle> findByPurchaseDateBetween(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.status = :status")
    Long countByStatus(@Param("status") Vehicle.VehicleStatus status);

    @Query("SELECT AVG(v.sellingPrice) FROM Vehicle v WHERE v.status = 'AVAILABLE'")
    BigDecimal getAverageSellingPrice();

    @Query("SELECT SUM(v.sellingPrice - v.purchasePrice) FROM Vehicle v WHERE v.status = 'SOLD'")
    BigDecimal getTotalPotentialProfit();

    @Query("SELECT v FROM Vehicle v WHERE " +
           "(:make IS NULL OR LOWER(v.make) LIKE LOWER(CONCAT('%', :make, '%'))) AND " +
           "(:model IS NULL OR LOWER(v.model) LIKE LOWER(CONCAT('%', :model, '%'))) AND " +
           "(:year IS NULL OR v.year = :year) AND " +
           "(:status IS NULL OR v.status = :status) AND " +
           "(:minPrice IS NULL OR v.sellingPrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR v.sellingPrice <= :maxPrice)")
    Page<Vehicle> findVehiclesWithFilters(@Param("make") String make,
                                         @Param("model") String model,
                                         @Param("year") Integer year,
                                         @Param("status") Vehicle.VehicleStatus status,
                                         @Param("minPrice") BigDecimal minPrice,
                                         @Param("maxPrice") BigDecimal maxPrice,
                                         Pageable pageable);

    @Query("SELECT v.make, COUNT(v) FROM Vehicle v GROUP BY v.make ORDER BY COUNT(v) DESC")
    List<Object[]> getVehicleCountByMake();

    @Query("SELECT v FROM Vehicle v WHERE v.mileage < :maxMileage AND v.status = 'AVAILABLE'")
    List<Vehicle> findLowMileageVehicles(@Param("maxMileage") Integer maxMileage);
}
