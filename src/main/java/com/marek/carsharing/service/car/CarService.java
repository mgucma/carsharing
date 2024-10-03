package com.marek.carsharing.service.car;

import com.marek.carsharing.dto.car.CarDetailsDto;
import com.marek.carsharing.dto.car.CarDto;
import com.marek.carsharing.dto.car.CarRequestDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface CarService {
    CarDto addCar(CarRequestDto createCarRequestDto);

    List<CarDto> getCars(Pageable pageable);

    CarDetailsDto getCarDetails(Long id);

    CarDto updateCar(Long id, CarRequestDto updateCarRequestDto);

    void delete(Long id);
}

