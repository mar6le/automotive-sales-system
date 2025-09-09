package com.automotive.sales.service;

import com.automotive.sales.model.Vehicle;
import com.automotive.sales.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public Vehicle createVehicle(Vehicle vehicle) {
        log.info("Creating new vehicle with VIN: {}", vehicle.getVin());
        
        // Check if VIN already exists
        if (vehicleRepository.findByVin(vehicle.getVin()).isPresent()) {
            throw new IllegalArgumentException("Vehicle with VIN " + vehicle.getVin() + " already exists");
        }
        
        // Set default values if not provided
        if (vehicle.getStatus() == null) {
            vehicle.setStatus(Vehicle.VehicleStatus.AVAILABLE);
        }
        
        if (vehicle.getCondition() == null) {
            vehicle.setCondition(Vehicle.VehicleCondition.NEW);
        }
        
        if (vehicle.getPurchaseDate() == null) {
            vehicle.setPurchaseDate(LocalDate.now());
        }
        
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle created successfully with ID: {}", savedVehicle.getId());
        return savedVehicle;
    }

    @Transactional(readOnly = true)
    public Optional<Vehicle> getVehicleById(Long id) {
        log.debug("Fetching vehicle with ID: {}", id);
        return vehicleRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Vehicle> getVehicleByVin(String vin) {
        log.debug("Fetching vehicle with VIN: {}", vin);
        return vehicleRepository.findByVin(vin);
    }

    @Transactional(readOnly = true)
    public List<Vehicle> getAllVehicles() {
        log.debug("Fetching all vehicles");
        return vehicleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Vehicle> getVehiclesWithFilters(String make, String model, Integer year,
                                               Vehicle.VehicleStatus status, BigDecimal minPrice,
                                               BigDecimal maxPrice, Pageable pageable) {
        log.debug("Fetching vehicles with filters - make: {}, model: {}, year: {}, status: {}", 
                 make, model, year, status);
        return vehicleRepository.findVehiclesWithFilters(make, model, year, status, minPrice, maxPrice, pageable);
    }

    @Transactional(readOnly = true)
    public List<Vehicle> getAvailableVehicles() {
        log.debug("Fetching available vehicles");
        return vehicleRepository.findByStatus(Vehicle.VehicleStatus.AVAILABLE);
    }

    @Transactional(readOnly = true)
    public List<Vehicle> getVehiclesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("Fetching vehicles in price range: {} - {}", minPrice, maxPrice);
        return vehicleRepository.findByPriceRange(minPrice, maxPrice);
    }

    public Vehicle updateVehicle(Long id, Vehicle vehicleDetails) {
        log.info("Updating vehicle with ID: {}", id);
        
        Vehicle existingVehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + id));

        // Update fields
        existingVehicle.setMake(vehicleDetails.getMake());
        existingVehicle.setModel(vehicleDetails.getModel());
        existingVehicle.setYear(vehicleDetails.getYear());
        existingVehicle.setColor(vehicleDetails.getColor());
        existingVehicle.setEngineType(vehicleDetails.getEngineType());
        existingVehicle.setTransmission(vehicleDetails.getTransmission());
        existingVehicle.setFuelType(vehicleDetails.getFuelType());
        existingVehicle.setMileage(vehicleDetails.getMileage());
        existingVehicle.setPurchasePrice(vehicleDetails.getPurchasePrice());
        existingVehicle.setSellingPrice(vehicleDetails.getSellingPrice());
        existingVehicle.setMsrp(vehicleDetails.getMsrp());
        existingVehicle.setStatus(vehicleDetails.getStatus());
        existingVehicle.setCondition(vehicleDetails.getCondition());
        existingVehicle.setDescription(vehicleDetails.getDescription());
        existingVehicle.setLocation(vehicleDetails.getLocation());

        Vehicle updatedVehicle = vehicleRepository.save(existingVehicle);
        log.info("Vehicle updated successfully with ID: {}", updatedVehicle.getId());
        return updatedVehicle;
    }

    public Vehicle updateVehicleStatus(Long id, Vehicle.VehicleStatus status) {
        log.info("Updating vehicle status for ID: {} to {}", id, status);
        
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + id));
        
        vehicle.setStatus(status);
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle status updated successfully");
        return updatedVehicle;
    }

    public void deleteVehicle(Long id) {
        log.info("Deleting vehicle with ID: {}", id);
        
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + id));
        
        // Check if vehicle has any sales
        if (vehicle.getSales() != null && !vehicle.getSales().isEmpty()) {
            throw new IllegalStateException("Cannot delete vehicle with existing sales records");
        }
        
        vehicleRepository.delete(vehicle);
        log.info("Vehicle deleted successfully");
    }

    @Transactional(readOnly = true)
    public List<Vehicle> getLowMileageVehicles(Integer maxMileage) {
        log.debug("Fetching low mileage vehicles with max mileage: {}", maxMileage);
        return vehicleRepository.findLowMileageVehicles(maxMileage);
    }

    @Transactional(readOnly = true)
    public BigDecimal getAverageSellingPrice() {
        log.debug("Calculating average selling price");
        return vehicleRepository.getAverageSellingPrice();
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalPotentialProfit() {
        log.debug("Calculating total potential profit");
        return vehicleRepository.getTotalPotentialProfit();
    }

    @Transactional(readOnly = true)
    public List<Object[]> getVehicleCountByMake() {
        log.debug("Getting vehicle count by make");
        return vehicleRepository.getVehicleCountByMake();
    }

    @Transactional(readOnly = true)
    public Long getVehicleCountByStatus(Vehicle.VehicleStatus status) {
        log.debug("Getting vehicle count for status: {}", status);
        return vehicleRepository.countByStatus(status);
    }

    public Vehicle reserveVehicle(Long id) {
        log.info("Reserving vehicle with ID: {}", id);
        return updateVehicleStatus(id, Vehicle.VehicleStatus.RESERVED);
    }

    public Vehicle markVehicleAsSold(Long id) {
        log.info("Marking vehicle as sold with ID: {}", id);
        return updateVehicleStatus(id, Vehicle.VehicleStatus.SOLD);
    }

    public Vehicle markVehicleForMaintenance(Long id) {
        log.info("Marking vehicle for maintenance with ID: {}", id);
        return updateVehicleStatus(id, Vehicle.VehicleStatus.MAINTENANCE);
    }

    public Vehicle makeVehicleAvailable(Long id) {
        log.info("Making vehicle available with ID: {}", id);
        return updateVehicleStatus(id, Vehicle.VehicleStatus.AVAILABLE);
    }
}
