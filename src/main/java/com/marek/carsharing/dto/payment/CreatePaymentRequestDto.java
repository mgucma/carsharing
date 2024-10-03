package com.marek.carsharing.dto.payment;

import jakarta.validation.constraints.PositiveOrZero;

public record CreatePaymentRequestDto(
        @PositiveOrZero
        Long rentalId
) {
}
