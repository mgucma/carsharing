package com.marek.carsharing.security.jwt;

import com.marek.carsharing.dto.user.login.LoginRequestDto;
import com.marek.carsharing.dto.user.login.LoginResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public LoginResponseDto authenticate(LoginRequestDto loginRequestDto) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.email(), loginRequestDto.password())
        );
        String token = jwtUtil.generateToken(authenticate.getName());

        return new LoginResponseDto(token);
    }
}

