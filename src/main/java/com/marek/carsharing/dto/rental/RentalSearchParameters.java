package com.marek.carsharing.dto.rental;

public record RentalSearchParameters(
        String[] userId,
        Boolean isActive
) {
}

