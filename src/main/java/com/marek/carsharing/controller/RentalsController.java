package com.marek.carsharing.controller;

import com.marek.carsharing.dto.rental.CreateRentalRequestDto;
import com.marek.carsharing.dto.rental.RentalDto;
import com.marek.carsharing.dto.rental.RentalSearchParameters;
import com.marek.carsharing.model.classes.User;
import com.marek.carsharing.service.rental.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalsController {
    private final RentalService rentalService;

    @Operation(summary = "Add a new rental")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('MANAGER', 'CUSTOMER')")
    @ResponseStatus(HttpStatus.CREATED)
    public RentalDto addRental(
            @RequestBody @Valid CreateRentalRequestDto createRentalRequestDto,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return rentalService.addRental(user, createRentalRequestDto);
    }

    @Operation(summary = "Get rentals by user ID and whether the rental is active - MANAGER only ")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('MANAGER')")
    @ResponseStatus(HttpStatus.OK)
    public List<RentalDto> getRentals(RentalSearchParameters parameters) {
        return rentalService.getRentals(parameters);
    }

    @Operation(summary = "Get specific rental - MANAGER only ")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('MANAGER')")
    @ResponseStatus(HttpStatus.OK)
    public RentalDto getRental(@PathVariable Long id) {
        return rentalService.getRental(id);
    }

    @Operation(summary = "Return a rental")
    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'CUSTOMER')")
    @ResponseStatus(HttpStatus.OK)
    public void returnRental(@PathVariable Long id,
                             Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        rentalService.returnRental(user, id);
    }
}


