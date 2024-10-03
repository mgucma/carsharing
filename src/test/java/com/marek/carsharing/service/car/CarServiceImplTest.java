package com.marek.carsharing.service.car;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.marek.carsharing.dto.car.CarDetailsDto;
import com.marek.carsharing.dto.car.CarDto;
import com.marek.carsharing.dto.car.CarRequestDto;
import com.marek.carsharing.mapper.CarMapper;
import com.marek.carsharing.model.classes.Car;
import com.marek.carsharing.model.enums.Type;
import com.marek.carsharing.repository.car.CarRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
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

@ExtendWith(MockitoExtension.class)
class CarServiceImplTest {
    public static final String MODEL = "model";
    public static final String BRAND = "brand";
    public static final String UNIVERSAL = "UNIVERSAL";
    public static final int INVENTORY = 10;
    public static final BigDecimal DAILY_FEE = BigDecimal.valueOf(10);
    public static final long ID = 1L;
    public static final Type UNIVERSAL_TYPE = Type.UNIVERSAL;
    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @InjectMocks
    private CarServiceImpl carService;

    @Test
    @DisplayName("addCar_withValidInput_returnsCarDto")
    void addCar_withValidInput_returnsCarDto() {
        // Given
        CarRequestDto carRequestDto = getCarRequestDto();
        Car car = getCar();
        CarDto carDto = getCarDto();

        when(carMapper.toEntity(carRequestDto)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(carDto);

        // When
        CarDto result = carService.addCar(carRequestDto);

        // Then
        assertNotNull(result);
        assertEquals(carDto, result);
        verify(carMapper, times(1)).toEntity(carRequestDto);
        verify(carRepository, times(1)).save(car);
        verify(carMapper, times(1)).toDto(car);
        verifyNoMoreInteractions(carMapper, carRepository);
    }

    @Test
    @DisplayName("getCars_withValidPageable_returnsListOfCarDto")
    void getCars_withValidPageable_returnsListOfCarDto() {
        // Given
        Car car = getCar();
        CarDto carDto = getCarDto();
        Pageable pageable = PageRequest.of(0, 10);
        List<Car> cars = List.of(car);
        Page<Car> carPage = new PageImpl<>(
                cars, pageable, cars.size()
        );

        when(carRepository.findAll(pageable)).thenReturn(carPage);
        when(carMapper.toDto(car)).thenReturn(carDto);

        // When
        List<CarDto> result = carService.getCars(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(carDto, result.get(0));
        verify(carRepository, times(1)).findAll(pageable);
        verify(carMapper, times(1)).toDto(car);
        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    @DisplayName("getCarDetails_withValidId_returnsCarDetailsDto")
    void getCarDetails_withValidId_returnsCarDetailsDto() {
        // Given
        Long carId = 1L;
        Car car = getCar();
        CarDetailsDto carDetailsDto = getCarDetailsDto();

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(carMapper.toDetailsDto(car)).thenReturn(carDetailsDto);

        // When
        CarDetailsDto result = carService.getCarDetails(carId);

        // Then
        assertNotNull(result);
        assertEquals(carDetailsDto, result);
        verify(carRepository, times(1)).findById(carId);
        verify(carMapper, times(1)).toDetailsDto(car);
        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    @DisplayName("getCarDetails_withInvalidId_throwsEntityNotFoundException")
    void getCarDetails_withInvalidId_throwsEntityNotFoundException() {
        // Given
        Long carId = -1L;

        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException entityNotFoundException =
                assertThrows(EntityNotFoundException.class, () -> carService.getCarDetails(carId));
        assertEquals(
                "Car not found with id: " + carId, entityNotFoundException.getMessage());
        verify(carRepository, times(1)).findById(carId);
        verifyNoMoreInteractions(carRepository);
    }

    @Test
    @DisplayName("updateCar_withValidIdAndRequest_returnsUpdatedCarDto")
    void updateCar_withValidIdAndRequest_returnsUpdatedCarDto() {
        // Given
        Long carId = 1L;
        CarRequestDto carRequestDto = getCarRequestDto();
        Car car = getCar();
        CarDto carDto = getCarDto();

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        doNothing().when(carMapper).updateDto(carRequestDto, car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(carDto);

        // When
        CarDto result = carService.updateCar(carId, carRequestDto);

        // Then
        assertNotNull(result);
        assertEquals(carDto, result);
        verify(carRepository, times(1)).findById(carId);
        verify(carMapper, times(1)).updateDto(carRequestDto, car);
        verify(carRepository, times(1)).save(car);
        verify(carMapper, times(1)).toDto(car);
        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    @DisplayName("updateCar_withInvalidId_throwsEntityNotFoundException")
    void updateCar_withInvalidId_throwsEntityNotFoundException() {
        // Given
        Long carId = -1L;
        CarRequestDto carRequestDto = getCarRequestDto();

        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException entityNotFoundException =
                assertThrows(EntityNotFoundException.class,
                        () -> carService.updateCar(carId, carRequestDto));

        assertEquals(
                "Car not found with id: " + carId, entityNotFoundException.getMessage());
        verify(carRepository, times(1)).findById(carId);
        verifyNoMoreInteractions(carRepository);
    }

    @Test
    @DisplayName("deleteCar_withValidId_deletesCar")
    void deleteCar_withValidId_deletesCar() {
        // Given
        Long carId = 1L;

        // When
        carService.delete(carId);

        // Then
        verify(carRepository, times(1)).deleteById(carId);
        verifyNoMoreInteractions(carRepository);
    }

    private CarRequestDto getCarRequestDto() {
        return new CarRequestDto(
                MODEL, BRAND, UNIVERSAL, INVENTORY, DAILY_FEE
        );
    }

    private Car getCar() {
        Car car = new Car();
        car.setId(ID);
        car.setModel(MODEL);
        car.setBrand(BRAND);
        car.setType(UNIVERSAL_TYPE);
        car.setInventory(INVENTORY);
        car.setDailyFee(DAILY_FEE);
        car.setDeleted(false);
        return car;
    }

    private CarDto getCarDto() {
        CarDto carDto = new CarDto();
        carDto.setId(ID);
        carDto.setModel(MODEL);
        carDto.setBrand(BRAND);
        carDto.setInventory(INVENTORY);
        carDto.setDailyFee(DAILY_FEE);
        return carDto;
    }

    private CarDetailsDto getCarDetailsDto() {
        CarDetailsDto carDetailsDto = new CarDetailsDto();
        carDetailsDto.setId(ID);
        carDetailsDto.setModel(MODEL);
        carDetailsDto.setBrand(BRAND);
        carDetailsDto.setType(UNIVERSAL);
        carDetailsDto.setInventory(INVENTORY);
        carDetailsDto.setDailyFee(DAILY_FEE);
        return carDetailsDto;
    }
}

