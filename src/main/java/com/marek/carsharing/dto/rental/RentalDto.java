package com.marek.carsharing.dto.rental;

import java.time.LocalDate;
import lombok.Data;

@Data
public class RentalDto {
    private Long id;
    private LocalDate rentalDate;
    private LocalDate actualReturnDate;
    private LocalDate returnDate;
    private Long carId;
}

