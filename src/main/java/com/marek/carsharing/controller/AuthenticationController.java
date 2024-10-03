package com.marek.carsharing.controller;

import com.marek.carsharing.dto.user.UserDto;
import com.marek.carsharing.dto.user.login.LoginRequestDto;
import com.marek.carsharing.dto.user.login.LoginResponseDto;
import com.marek.carsharing.dto.user.registration.RegisterRequestDto;
import com.marek.carsharing.security.jwt.AuthService;
import com.marek.carsharing.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final UserService userService;
    private final AuthService authService;

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto register(
            @RequestBody @Valid RegisterRequestDto requestDto) {
        return userService.register(requestDto);
    }

    @Operation(summary = "User login to obtain JWT tokens")
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponseDto login(
            @RequestBody @Valid LoginRequestDto requestDto) {
        return authService.authenticate(requestDto);
    }
}


