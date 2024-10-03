package com.marek.carsharing.dto.rental;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

public record CreateRentalRequestDto(
        @NotNull
        LocalDate rentalDate,
        @NotNull
        LocalDate returnDate,
        @PositiveOrZero
        Long carId
) {
}
