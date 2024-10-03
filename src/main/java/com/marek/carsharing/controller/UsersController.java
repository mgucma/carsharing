package com.marek.carsharing.controller;

import com.marek.carsharing.dto.user.UpdateUserRequestDto;
import com.marek.carsharing.dto.user.UserDto;
import com.marek.carsharing.model.classes.User;
import com.marek.carsharing.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {
    private final UserService userService;

    @Operation(summary = "Update user role - MANAGER only ",
            description = """
                    If user have Role = Manager then Manager => Customer,\n 
                    If user have Role = Customer then Customer => Manager, 
                    """)
    @PutMapping("/{id}/role")
    @PreAuthorize("hasAnyAuthority('MANAGER')")
    @ResponseStatus(HttpStatus.OK)
    public UserDto updateUserRole(@PathVariable Long id) {
        return userService.updateRole(id);
    }

    @Operation(summary = "Get my profile info")
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public UserDto getMyProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return userService.getMyProfile(user);
    }

    @Operation(summary = "Update my profile info")
    @PutMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public UserDto updateMyProfile(Authentication authentication,
                                   @RequestBody @Valid UpdateUserRequestDto request) {
        User user = (User) authentication.getPrincipal();
        return userService.updateMyProfile(user, request);
    }
}
