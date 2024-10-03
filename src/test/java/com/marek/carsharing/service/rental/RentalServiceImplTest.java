package com.marek.carsharing.service.rental;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.marek.carsharing.dto.rental.CreateRentalRequestDto;
import com.marek.carsharing.dto.rental.RentalDto;
import com.marek.carsharing.mapper.RentalMapper;
import com.marek.carsharing.model.classes.Car;
import com.marek.carsharing.model.classes.Rental;
import com.marek.carsharing.model.classes.User;
import com.marek.carsharing.model.enums.Role;
import com.marek.carsharing.model.enums.Type;
import com.marek.carsharing.repository.car.CarRepository;
import com.marek.carsharing.repository.rental.RentalRepository;
import com.marek.carsharing.service.notification.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RentalServiceImplTest {
    public static final long ID = 1L;
    public static final String EMAIL = "admin@simpleart.eu";
    public static final String FIRST_NAME = "B";
    public static final String LAST_NAME = "W";
    public static final String PASSWORD = "password";
    public static final Role ROLE = Role.MANAGER;
    public static final LocalDate RENTAL_DATE = LocalDate.of(2024, 8, 1);
    public static final LocalDate RETURN_DATE = LocalDate.of(2024, 8, 5);
    public static final LocalDate ACTUAL_RETURN_DATE = LocalDate.of(2024, 8, 5);
    public static final String MODEL = "model";
    public static final String BRAND = "brand";
    public static final int INVENTORY = 10;
    public static final BigDecimal DAILY_FEE = BigDecimal.valueOf(10);
    public static final Type UNIVERSAL_TYPE = Type.UNIVERSAL;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private RentalMapper rentalMapper;

    @Mock
    private CarRepository carRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RentalServiceImpl rentalService;

    @Test
    @DisplayName("addRental_withValidInput_returnsRentalDto")
    void addRental_withValidInput_returnsRentalDto() {
        // Given
        User user = getUser();
        CreateRentalRequestDto requestDto = getCreateRentalRequestDto();
        Rental rental = getRental();
        Car car = getCar();
        RentalDto rentalDto = getRentalDto();

        when(carRepository.findById(car.getId())).thenReturn(Optional.of(car));
        when(rentalMapper.toEntity(requestDto)).thenReturn(rental);
        when(rentalRepository.save(rental)).thenReturn(rental);
        when(rentalMapper.toDto(rental)).thenReturn(rentalDto);

        // When
        RentalDto result = rentalService.addRental(user, requestDto);

        // Then
        assertNotNull(result);
        assertEquals(rentalDto, result);
        verify(carRepository, times(1)).findById(car.getId());
        verify(rentalRepository, times(1)).save(rental);
        verify(rentalMapper, times(1)).toEntity(requestDto);
        verify(rentalMapper, times(1)).toDto(rental);
        verify(notificationService, times(1)).notifyNewRentalsCreated(anyString());
        verifyNoMoreInteractions(
                carRepository, rentalRepository, rentalMapper, notificationService);
    }

    @Test
    @DisplayName("getRental_withValidId_returnsRentalDto")
    void getRental_withValidId_returnsRentalDto() {
        // Given
        Long rentalId = 1L;
        Rental rental = getRental();
        RentalDto rentalDto = getRentalDto();

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(rentalMapper.toDto(rental)).thenReturn(rentalDto);

        // When
        RentalDto result = rentalService.getRental(rentalId);

        // Then
        assertNotNull(result);
        assertEquals(rentalDto, result);
        verify(rentalRepository, times(1)).findById(rentalId);
        verify(rentalMapper, times(1)).toDto(rental);
        verifyNoMoreInteractions(rentalRepository, rentalMapper);
    }

    @Test
    @DisplayName("getRental_withInvalidId_throwsEntityNotFoundException")
    void getRental_withInvalidId_throwsEntityNotFoundException() {
        // Given
        Long rentalId = -1L;

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException entityNotFoundException =
                Assertions.assertThrows(
                        EntityNotFoundException.class, () -> rentalService.getRental(rentalId));
        Assertions.assertEquals("Rental with id " + rentalId + " not found",
                entityNotFoundException.getMessage());
        verify(rentalRepository, times(1)).findById(rentalId);
        verifyNoMoreInteractions(rentalRepository);
    }

    @Test
    @DisplayName("returnRental_withValidInput_updatesCarInventoryAndSavesRental")
    void returnRental_withValidInput_updatesCarInventoryAndSavesRental() {
        // Given
        User user = getUser();
        Long rentalId = 1L;
        Rental rental = getRental();
        Car car = getCar();

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(carRepository.findById(car.getId())).thenReturn(Optional.of(car));

        // When
        rentalService.returnRental(user, rentalId);

        // Then
        verify(rentalRepository, times(1)).findById(rentalId);
        verify(carRepository, times(1)).findById(anyLong());
        verify(rentalRepository, times(1)).save(rental);
        verifyNoMoreInteractions(rentalRepository, carRepository);
    }

    @Test
    @DisplayName("returnRental_withInvalidRentalId_throwsEntityNotFoundException")
    void returnRental_withInvalidRentalId_throwsEntityNotFoundException() {
        // Given
        User user = getUser();
        Long rentalId = -1L;

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException entityNotFoundException =
                Assertions.assertThrows(EntityNotFoundException.class,
                        () -> rentalService.returnRental(user, rentalId));
        Assertions.assertEquals("Rental with id " + rentalId + " not found",
                entityNotFoundException.getMessage());
        verify(rentalRepository, times(1)).findById(rentalId);
        verifyNoMoreInteractions(rentalRepository);
    }

    private User getUser() {
        User user = new User();
        user.setId(ID);
        user.setEmail(EMAIL);
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setPassword(PASSWORD);
        user.setRole(ROLE);
        user.setDeleted(false);
        return user;
    }

    private CreateRentalRequestDto getCreateRentalRequestDto() {
        return new CreateRentalRequestDto(
                RENTAL_DATE, RETURN_DATE, ID
        );
    }

    private Rental getRental() {
        Rental rental = new Rental();
        rental.setId(ID);
        rental.setRentalDate(RENTAL_DATE);
        rental.setReturnDate(RETURN_DATE);
        rental.setActualReturnDate(ACTUAL_RETURN_DATE);
        rental.setCarId(ID);
        rental.setUserId(ID);
        rental.setDeleted(false);
        return rental;
    }

    private RentalDto getRentalDto() {
        RentalDto rentalDto = new RentalDto();
        rentalDto.setId(ID);
        rentalDto.setRentalDate(RENTAL_DATE);
        rentalDto.setActualReturnDate(ACTUAL_RETURN_DATE);
        rentalDto.setReturnDate(RETURN_DATE);
        rentalDto.setCarId(ID);
        return rentalDto;
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
}

