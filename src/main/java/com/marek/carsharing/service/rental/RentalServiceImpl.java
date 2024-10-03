package com.marek.carsharing.service.rental;

import com.marek.carsharing.dto.rental.CreateRentalRequestDto;
import com.marek.carsharing.dto.rental.RentalDto;
import com.marek.carsharing.dto.rental.RentalSearchParameters;
import com.marek.carsharing.mapper.RentalMapper;
import com.marek.carsharing.model.classes.Car;
import com.marek.carsharing.model.classes.Rental;
import com.marek.carsharing.model.classes.User;
import com.marek.carsharing.repository.car.CarRepository;
import com.marek.carsharing.repository.rental.RentalRepository;
import com.marek.carsharing.repository.rental.provider.RentalSpecificationBuilder;
import com.marek.carsharing.service.notification.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {
    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;
    private final CarRepository carRepository;
    private final RentalSpecificationBuilder rentalSpecificationBuilder;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public RentalDto addRental(
            User user, CreateRentalRequestDto createRentalRequestDto) {
        updateCarInventoryAfterRent(createRentalRequestDto);
        notificationService.notifyNewRentalsCreated(
                "User with id " + user.getId()
                        + " rent a car with id " + createRentalRequestDto.carId()
                        + " from " + createRentalRequestDto.rentalDate()
                        + " to " + createRentalRequestDto.returnDate()
        );
        Rental entity = rentalMapper.toEntity(createRentalRequestDto);
        entity.setUserId(user.getId());
        return rentalMapper.toDto(
                rentalRepository.save(entity)
        );
    }

    private void updateCarInventoryAfterRent(CreateRentalRequestDto createRentalRequestDto) {
        Car car = carRepository.findById(createRentalRequestDto.carId()).orElseThrow(
                () -> new EntityNotFoundException(
                        "Car with id " + createRentalRequestDto.carId() + " not found")
        );
        car.setInventory(car.getInventory() - 1);
    }

    @Override
    public List<RentalDto> getRentals(RentalSearchParameters rentalSearchParameters) {
        Specification<Rental> build = rentalSpecificationBuilder.build(rentalSearchParameters);
        return rentalRepository.findAll(build).stream()
                .map(rentalMapper::toDto)
                .toList();
    }

    @Override
    public RentalDto getRental(Long id) {
        return rentalMapper.toDto(
                rentalRepository.findById(id).orElseThrow(
                        () -> new EntityNotFoundException("Rental with id " + id + " not found")
                )
        );
    }

    @Override
    @Transactional
    public void returnRental(User user, Long id) {
        Rental rental = rentalRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Rental with id " + id + " not found")
        );
        checkIfThisRentIsForCorrectUser(user, rental);

        rental.setActualReturnDate(LocalDate.now());
        notificationForOverdueRent(rental);

        rental.setId(id);
        Long carId = rental.getCarId();
        Car car = carRepository.findById(carId).orElseThrow(
                () -> new EntityNotFoundException("Car with id " + carId + " not found")
        );
        car.setInventory(car.getInventory() + 1);

        rentalRepository.save(rental);
    }

    private void notificationForOverdueRent(Rental rental) {
        if (rental.getReturnDate().isBefore(LocalDate.now())) {
            notificationService.notifyOverdueRentals(
                    "User with id " + rental.getUserId()
                            + " returned the car late, car id " + rental.getCarId()
            );
        }
    }

    private static void checkIfThisRentIsForCorrectUser(User user, Rental rental) {
        if (!user.getId().equals(rental.getUserId())) {
            throw new EntityNotFoundException(
                    "User with id " + user.getId()
                            + " does not belong to this Rental"
            );
        }
    }
}
