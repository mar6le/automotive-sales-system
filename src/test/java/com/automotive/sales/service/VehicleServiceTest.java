package com.automotive.sales.service;

import com.automotive.sales.model.Vehicle;
import com.automotive.sales.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Vehicle Service Tests")
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private Vehicle testVehicle;

    @BeforeEach
    void setUp() {
        testVehicle = Vehicle.builder()
                .vin("1HGBH41JXMN109186")
                .make("Toyota")
                .model("Camry")
                .year(2023)
                .color("White")
                .engineType("2.5L I4")
                .transmission("Automatic")
                .fuelType("Gasoline")
                .mileage(15000)
                .purchasePrice(new BigDecimal("25000.00"))
                .sellingPrice(new BigDecimal("28000.00"))
                .msrp(new BigDecimal("30000.00"))
                .status(Vehicle.VehicleStatus.AVAILABLE)
                .condition(Vehicle.VehicleCondition.USED)
                .purchaseDate(LocalDate.now().minusMonths(2))
                .description("Well-maintained vehicle")
                .location("Lot A")
                .build();
        // Set ID manually for testing purposes since it's auto-generated
        testVehicle.setId(1L);
    }

    @Test
    @DisplayName("Should create vehicle successfully with valid data")
    void createVehicle_WithValidData_ShouldReturnCreatedVehicle() {
        // Given
        when(vehicleRepository.findByVin(testVehicle.getVin())).thenReturn(Optional.empty());
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(testVehicle);

        // When
        Vehicle result = vehicleService.createVehicle(testVehicle);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getVin()).isEqualTo(testVehicle.getVin());
        assertThat(result.getMake()).isEqualTo(testVehicle.getMake());
        assertThat(result.getModel()).isEqualTo(testVehicle.getModel());
        verify(vehicleRepository).findByVin(testVehicle.getVin());
        verify(vehicleRepository).save(testVehicle);
    }

    @Test
    @DisplayName("Should throw exception when creating vehicle with duplicate VIN")
    void createVehicle_WithDuplicateVin_ShouldThrowException() {
        // Given
        when(vehicleRepository.findByVin(testVehicle.getVin())).thenReturn(Optional.of(testVehicle));

        // When & Then
        assertThatThrownBy(() -> vehicleService.createVehicle(testVehicle))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Vehicle with VIN " + testVehicle.getVin() + " already exists");

        verify(vehicleRepository).findByVin(testVehicle.getVin());
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should set default values when creating vehicle")
    void createVehicle_WithMissingDefaults_ShouldSetDefaultValues() {
        // Given
        Vehicle vehicleWithoutDefaults = Vehicle.builder()
                .vin("1HGBH41JXMN109187")
                .make("Honda")
                .model("Civic")
                .year(2023)
                .purchasePrice(new BigDecimal("20000.00"))
                .sellingPrice(new BigDecimal("23000.00"))
                .build();

        when(vehicleRepository.findByVin(vehicleWithoutDefaults.getVin())).thenReturn(Optional.empty());
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        // When
        Vehicle result = vehicleService.createVehicle(vehicleWithoutDefaults);

        // Then
        assertThat(result.getStatus()).isEqualTo(Vehicle.VehicleStatus.AVAILABLE);
        assertThat(result.getCondition()).isEqualTo(Vehicle.VehicleCondition.NEW);
        assertThat(result.getPurchaseDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("Should find vehicle by ID successfully")
    void getVehicleById_WithValidId_ShouldReturnVehicle() {
        // Given
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));

        // When
        Optional<Vehicle> result = vehicleService.getVehicleById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testVehicle);
        verify(vehicleRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return empty when vehicle not found by ID")
    void getVehicleById_WithInvalidId_ShouldReturnEmpty() {
        // Given
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Vehicle> result = vehicleService.getVehicleById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(vehicleRepository).findById(999L);
    }

    @Test
    @DisplayName("Should find vehicle by VIN successfully")
    void getVehicleByVin_WithValidVin_ShouldReturnVehicle() {
        // Given
        when(vehicleRepository.findByVin(testVehicle.getVin())).thenReturn(Optional.of(testVehicle));

        // When
        Optional<Vehicle> result = vehicleService.getVehicleByVin(testVehicle.getVin());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testVehicle);
        verify(vehicleRepository).findByVin(testVehicle.getVin());
    }

    @Test
    @DisplayName("Should return all vehicles")
    void getAllVehicles_ShouldReturnAllVehicles() {
        // Given
        List<Vehicle> vehicles = Arrays.asList(testVehicle, createSecondVehicle());
        when(vehicleRepository.findAll()).thenReturn(vehicles);

        // When
        List<Vehicle> result = vehicleService.getAllVehicles();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(vehicles);
        verify(vehicleRepository).findAll();
    }

    @Test
    @DisplayName("Should return vehicles with filters")
    void getVehiclesWithFilters_ShouldReturnFilteredVehicles() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Vehicle> vehiclePage = new PageImpl<>(Arrays.asList(testVehicle));
        when(vehicleRepository.findVehiclesWithFilters(
                eq("Toyota"), eq("Camry"), eq(2023), eq(Vehicle.VehicleStatus.AVAILABLE),
                any(BigDecimal.class), any(BigDecimal.class), eq(pageable)))
                .thenReturn(vehiclePage);

        // When
        Page<Vehicle> result = vehicleService.getVehiclesWithFilters(
                "Toyota", "Camry", 2023, Vehicle.VehicleStatus.AVAILABLE,
                new BigDecimal("20000"), new BigDecimal("30000"), pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testVehicle);
    }

    @Test
    @DisplayName("Should update vehicle successfully")
    void updateVehicle_WithValidData_ShouldReturnUpdatedVehicle() {
        // Given
        Vehicle updatedDetails = Vehicle.builder()
                .make("Toyota")
                .model("Camry")
                .year(2023)
                .color("Blue")
                .mileage(16000)
                .sellingPrice(new BigDecimal("27000.00"))
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Vehicle result = vehicleService.updateVehicle(1L, updatedDetails);

        // Then
        assertThat(result.getColor()).isEqualTo("Blue");
        assertThat(result.getMileage()).isEqualTo(16000);
        assertThat(result.getSellingPrice()).isEqualTo(new BigDecimal("27000.00"));
        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository).save(testVehicle);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent vehicle")
    void updateVehicle_WithInvalidId_ShouldThrowException() {
        // Given
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> vehicleService.updateVehicle(999L, testVehicle))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Vehicle not found with ID: 999");

        verify(vehicleRepository).findById(999L);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should update vehicle status successfully")
    void updateVehicleStatus_WithValidData_ShouldReturnUpdatedVehicle() {
        // Given
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Vehicle result = vehicleService.updateVehicleStatus(1L, Vehicle.VehicleStatus.SOLD);

        // Then
        assertThat(result.getStatus()).isEqualTo(Vehicle.VehicleStatus.SOLD);
        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository).save(testVehicle);
    }

    @Test
    @DisplayName("Should delete vehicle successfully when no sales exist")
    void deleteVehicle_WithNoSales_ShouldDeleteSuccessfully() {
        // Given
        testVehicle.setSales(Arrays.asList()); // Empty sales list
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));

        // When
        vehicleService.deleteVehicle(1L);

        // Then
        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository).delete(testVehicle);
    }

    @Test
    @DisplayName("Should throw exception when deleting vehicle with existing sales")
    void deleteVehicle_WithExistingSales_ShouldThrowException() {
        // Given
        testVehicle.setSales(Arrays.asList(mock(com.automotive.sales.model.Sale.class)));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));

        // When & Then
        assertThatThrownBy(() -> vehicleService.deleteVehicle(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete vehicle with existing sales records");

        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository, never()).delete(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should return available vehicles")
    void getAvailableVehicles_ShouldReturnOnlyAvailableVehicles() {
        // Given
        List<Vehicle> availableVehicles = Arrays.asList(testVehicle);
        when(vehicleRepository.findByStatus(Vehicle.VehicleStatus.AVAILABLE)).thenReturn(availableVehicles);

        // When
        List<Vehicle> result = vehicleService.getAvailableVehicles();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Vehicle.VehicleStatus.AVAILABLE);
        verify(vehicleRepository).findByStatus(Vehicle.VehicleStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Should return vehicles by price range")
    void getVehiclesByPriceRange_ShouldReturnVehiclesInRange() {
        // Given
        BigDecimal minPrice = new BigDecimal("25000");
        BigDecimal maxPrice = new BigDecimal("30000");
        List<Vehicle> vehiclesInRange = Arrays.asList(testVehicle);
        when(vehicleRepository.findByPriceRange(minPrice, maxPrice)).thenReturn(vehiclesInRange);

        // When
        List<Vehicle> result = vehicleService.getVehiclesByPriceRange(minPrice, maxPrice);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSellingPrice()).isBetween(minPrice, maxPrice);
        verify(vehicleRepository).findByPriceRange(minPrice, maxPrice);
    }

    @Test
    @DisplayName("Should reserve vehicle successfully")
    void reserveVehicle_WithValidId_ShouldReturnReservedVehicle() {
        // Given
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Vehicle result = vehicleService.reserveVehicle(1L);

        // Then
        assertThat(result.getStatus()).isEqualTo(Vehicle.VehicleStatus.RESERVED);
        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository).save(testVehicle);
    }

    @Test
    @DisplayName("Should mark vehicle as sold successfully")
    void markVehicleAsSold_WithValidId_ShouldReturnSoldVehicle() {
        // Given
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Vehicle result = vehicleService.markVehicleAsSold(1L);

        // Then
        assertThat(result.getStatus()).isEqualTo(Vehicle.VehicleStatus.SOLD);
        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository).save(testVehicle);
    }

    @Test
    @DisplayName("Should get vehicle statistics")
    void getVehicleStatistics_ShouldReturnCorrectStatistics() {
        // Given
        when(vehicleRepository.getAverageSellingPrice()).thenReturn(new BigDecimal("25000.00"));
        when(vehicleRepository.getTotalPotentialProfit()).thenReturn(new BigDecimal("50000.00"));
        when(vehicleRepository.countByStatus(Vehicle.VehicleStatus.AVAILABLE)).thenReturn(10L);
        when(vehicleRepository.countByStatus(Vehicle.VehicleStatus.SOLD)).thenReturn(5L);

        // When
        BigDecimal avgPrice = vehicleService.getAverageSellingPrice();
        BigDecimal totalProfit = vehicleService.getTotalPotentialProfit();
        Long availableCount = vehicleService.getVehicleCountByStatus(Vehicle.VehicleStatus.AVAILABLE);
        Long soldCount = vehicleService.getVehicleCountByStatus(Vehicle.VehicleStatus.SOLD);

        // Then
        assertThat(avgPrice).isEqualTo(new BigDecimal("25000.00"));
        assertThat(totalProfit).isEqualTo(new BigDecimal("50000.00"));
        assertThat(availableCount).isEqualTo(10L);
        assertThat(soldCount).isEqualTo(5L);
    }

    private Vehicle createSecondVehicle() {
        Vehicle vehicle = Vehicle.builder()
                .vin("1HGBH41JXMN109187")
                .make("Honda")
                .model("Civic")
                .year(2023)
                .color("Black")
                .purchasePrice(new BigDecimal("20000.00"))
                .sellingPrice(new BigDecimal("23000.00"))
                .status(Vehicle.VehicleStatus.AVAILABLE)
                .condition(Vehicle.VehicleCondition.NEW)
                .build();
        // Set ID manually for testing purposes since it's auto-generated
        vehicle.setId(2L);
        return vehicle;
    }
}
