package com.marek.carsharing.mapper;

import com.marek.carsharing.config.MapperConfig;
import com.marek.carsharing.dto.rental.CreateRentalRequestDto;
import com.marek.carsharing.dto.rental.RentalDto;
import com.marek.carsharing.model.classes.Rental;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface RentalMapper {

    RentalDto toDto(Rental rental);

    Rental toEntity(CreateRentalRequestDto rentalDto);
}
