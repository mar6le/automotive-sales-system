package com.automotive.sales.controller;

import com.automotive.sales.model.Vehicle;
import com.automotive.sales.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vehicle Management", description = "APIs for managing vehicle inventory")
public class VehicleController {

    private final VehicleService vehicleService;

    @Operation(summary = "Create a new vehicle", description = "Add a new vehicle to the inventory")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vehicle created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Vehicle with VIN already exists")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Vehicle> createVehicle(@Valid @RequestBody Vehicle vehicle) {
        log.info("Creating new vehicle with VIN: {}", vehicle.getVin());
        Vehicle createdVehicle = vehicleService.createVehicle(vehicle);
        return new ResponseEntity<>(createdVehicle, HttpStatus.CREATED);
    }

    @Operation(summary = "Get vehicle by ID", description = "Retrieve a specific vehicle by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle found"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        log.debug("Fetching vehicle with ID: {}", id);
        return vehicleService.getVehicleById(id)
                .map(vehicle -> ResponseEntity.ok(vehicle))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get vehicle by VIN", description = "Retrieve a specific vehicle by its VIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle found"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    @GetMapping("/vin/{vin}")
    public ResponseEntity<Vehicle> getVehicleByVin(
            @Parameter(description = "Vehicle VIN") @PathVariable String vin) {
        log.debug("Fetching vehicle with VIN: {}", vin);
        return vehicleService.getVehicleByVin(vin)
                .map(vehicle -> ResponseEntity.ok(vehicle))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all vehicles", description = "Retrieve all vehicles with optional filtering")
    @ApiResponse(responseCode = "200", description = "Vehicles retrieved successfully")
    @GetMapping
    public ResponseEntity<Page<Vehicle>> getAllVehicles(
            @Parameter(description = "Vehicle make") @RequestParam(required = false) String make,
            @Parameter(description = "Vehicle model") @RequestParam(required = false) String model,
            @Parameter(description = "Vehicle year") @RequestParam(required = false) Integer year,
            @Parameter(description = "Vehicle status") @RequestParam(required = false) Vehicle.VehicleStatus status,
            @Parameter(description = "Minimum price") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam(required = false) BigDecimal maxPrice,
            Pageable pageable) {
        
        log.debug("Fetching vehicles with filters - make: {}, model: {}, year: {}, status: {}", 
                 make, model, year, status);
        
        Page<Vehicle> vehicles = vehicleService.getVehiclesWithFilters(
                make, model, year, status, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(vehicles);
    }

    @Operation(summary = "Get available vehicles", description = "Retrieve all available vehicles")
    @ApiResponse(responseCode = "200", description = "Available vehicles retrieved successfully")
    @GetMapping("/available")
    public ResponseEntity<List<Vehicle>> getAvailableVehicles() {
        log.debug("Fetching available vehicles");
        List<Vehicle> vehicles = vehicleService.getAvailableVehicles();
        return ResponseEntity.ok(vehicles);
    }

    @Operation(summary = "Get vehicles by price range", description = "Retrieve vehicles within a specific price range")
    @ApiResponse(responseCode = "200", description = "Vehicles retrieved successfully")
    @GetMapping("/price-range")
    public ResponseEntity<List<Vehicle>> getVehiclesByPriceRange(
            @Parameter(description = "Minimum price") @RequestParam BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam BigDecimal maxPrice) {
        
        log.debug("Fetching vehicles in price range: {} - {}", minPrice, maxPrice);
        List<Vehicle> vehicles = vehicleService.getVehiclesByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(vehicles);
    }

    @Operation(summary = "Update vehicle", description = "Update an existing vehicle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle updated successfully"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Vehicle> updateVehicle(
            @Parameter(description = "Vehicle ID") @PathVariable Long id,
            @Valid @RequestBody Vehicle vehicleDetails) {
        
        log.info("Updating vehicle with ID: {}", id);
        try {
            Vehicle updatedVehicle = vehicleService.updateVehicle(id, vehicleDetails);
            return ResponseEntity.ok(updatedVehicle);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Update vehicle status", description = "Update the status of a vehicle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SALESPERSON')")
    public ResponseEntity<Vehicle> updateVehicleStatus(
            @Parameter(description = "Vehicle ID") @PathVariable Long id,
            @Parameter(description = "New status") @RequestParam Vehicle.VehicleStatus status) {
        
        log.info("Updating vehicle status for ID: {} to {}", id, status);
        try {
            Vehicle updatedVehicle = vehicleService.updateVehicleStatus(id, status);
            return ResponseEntity.ok(updatedVehicle);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Reserve vehicle", description = "Reserve a vehicle for a potential sale")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle reserved successfully"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    @PatchMapping("/{id}/reserve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SALESPERSON')")
    public ResponseEntity<Vehicle> reserveVehicle(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        
        log.info("Reserving vehicle with ID: {}", id);
        try {
            Vehicle reservedVehicle = vehicleService.reserveVehicle(id);
            return ResponseEntity.ok(reservedVehicle);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Mark vehicle as sold", description = "Mark a vehicle as sold")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle marked as sold successfully"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    @PatchMapping("/{id}/sold")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Vehicle> markVehicleAsSold(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        
        log.info("Marking vehicle as sold with ID: {}", id);
        try {
            Vehicle soldVehicle = vehicleService.markVehicleAsSold(id);
            return ResponseEntity.ok(soldVehicle);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete vehicle", description = "Delete a vehicle from inventory")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Vehicle deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete vehicle with existing sales")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVehicle(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        
        log.info("Deleting vehicle with ID: {}", id);
        try {
            vehicleService.deleteVehicle(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @Operation(summary = "Get low mileage vehicles", description = "Retrieve vehicles with mileage below specified threshold")
    @ApiResponse(responseCode = "200", description = "Low mileage vehicles retrieved successfully")
    @GetMapping("/low-mileage")
    public ResponseEntity<List<Vehicle>> getLowMileageVehicles(
            @Parameter(description = "Maximum mileage threshold") @RequestParam Integer maxMileage) {
        
        log.debug("Fetching low mileage vehicles with max mileage: {}", maxMileage);
        List<Vehicle> vehicles = vehicleService.getLowMileageVehicles(maxMileage);
        return ResponseEntity.ok(vehicles);
    }

    @Operation(summary = "Get vehicle statistics", description = "Retrieve vehicle inventory statistics")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    @GetMapping("/statistics")
    public ResponseEntity<VehicleStatistics> getVehicleStatistics() {
        log.debug("Fetching vehicle statistics");
        
        VehicleStatistics stats = VehicleStatistics.builder()
                .averageSellingPrice(vehicleService.getAverageSellingPrice())
                .totalPotentialProfit(vehicleService.getTotalPotentialProfit())
                .vehicleCountByMake(vehicleService.getVehicleCountByMake())
                .availableCount(vehicleService.getVehicleCountByStatus(Vehicle.VehicleStatus.AVAILABLE))
                .soldCount(vehicleService.getVehicleCountByStatus(Vehicle.VehicleStatus.SOLD))
                .reservedCount(vehicleService.getVehicleCountByStatus(Vehicle.VehicleStatus.RESERVED))
                .maintenanceCount(vehicleService.getVehicleCountByStatus(Vehicle.VehicleStatus.MAINTENANCE))
                .build();
        
        return ResponseEntity.ok(stats);
    }

    @lombok.Data
    @lombok.Builder
    public static class VehicleStatistics {
        private BigDecimal averageSellingPrice;
        private BigDecimal totalPotentialProfit;
        private List<Object[]> vehicleCountByMake;
        private Long availableCount;
        private Long soldCount;
        private Long reservedCount;
        private Long maintenanceCount;
    }
}
