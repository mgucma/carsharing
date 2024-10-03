package com.marek.carsharing.mapper;

import com.marek.carsharing.config.MapperConfig;
import com.marek.carsharing.dto.car.CarDetailsDto;
import com.marek.carsharing.dto.car.CarDto;
import com.marek.carsharing.dto.car.CarRequestDto;
import com.marek.carsharing.model.classes.Car;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface CarMapper {

    CarDto toDto(Car car);

    CarDetailsDto toDetailsDto(Car car);

    Car toEntity(CarRequestDto carDto);

    void updateDto(CarRequestDto carDto, @MappingTarget Car car);
}
