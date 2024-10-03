package com.marek.carsharing.service.car;

import com.marek.carsharing.dto.car.CarDetailsDto;
import com.marek.carsharing.dto.car.CarDto;
import com.marek.carsharing.dto.car.CarRequestDto;
import com.marek.carsharing.mapper.CarMapper;
import com.marek.carsharing.model.classes.Car;
import com.marek.carsharing.model.enums.Type;
import com.marek.carsharing.repository.car.CarRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    @Transactional
    public CarDto addCar(CarRequestDto createCarRequestDto) {
        Car car = carMapper.toEntity(createCarRequestDto);
        setTypeForCar(createCarRequestDto, car);
        return carMapper.toDto(carRepository.save(car));
    }

    @Override
    public List<CarDto> getCars(Pageable pageable) {
        return carRepository.findAll(pageable).stream()
                .map(carMapper::toDto)
                .toList();
    }

    @Override
    public CarDetailsDto getCarDetails(Long id) {
        return carMapper.toDetailsDto(
                carRepository.findById(id).orElseThrow(
                        () -> new EntityNotFoundException("Car not found with id: " + id)
                ));
    }

    @Override
    @Transactional
    public CarDto updateCar(Long id, @Valid CarRequestDto updateCarRequestDto) {
        Car car = carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(
                        "Car not found with id: " + id
                )
        );
        carMapper.updateDto(updateCarRequestDto, car);

        setTypeForCar(updateCarRequestDto, car);

        return carMapper.toDto(carRepository.save(car));

    }

    @Override
    public void delete(Long id) {
        carRepository.deleteById(id);
    }

    private static void setTypeForCar(CarRequestDto createCarRequestDto, Car car) {
        switch (createCarRequestDto.type().toUpperCase()) {
            case "SEDAN" -> car.setType(Type.SEDAN);
            case "SUV" -> car.setType(Type.SUV);
            case "HATCHBACK" -> car.setType(Type.HATCHBACK);
            default -> car.setType(Type.UNIVERSAL);
        }
    }
}

