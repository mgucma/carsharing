package com.marek.carsharing.service.rental;

import com.marek.carsharing.dto.rental.CreateRentalRequestDto;
import com.marek.carsharing.dto.rental.RentalDto;
import com.marek.carsharing.dto.rental.RentalSearchParameters;
import com.marek.carsharing.model.classes.User;
import java.util.List;

public interface RentalService {
    RentalDto addRental(User user, CreateRentalRequestDto createRentalRequestDto);

    List<RentalDto> getRentals(RentalSearchParameters rentalSearchParameters);

    RentalDto getRental(Long id);

    void returnRental(User user, Long id);
}

