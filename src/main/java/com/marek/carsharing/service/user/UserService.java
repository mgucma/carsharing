package com.marek.carsharing.service.user;

import com.marek.carsharing.dto.user.UpdateUserRequestDto;
import com.marek.carsharing.dto.user.UserDto;
import com.marek.carsharing.dto.user.registration.RegisterRequestDto;
import com.marek.carsharing.model.classes.User;

public interface UserService {
    UserDto register(RegisterRequestDto requestDto);

    UserDto updateRole(Long id);

    UserDto getMyProfile(User user);

    UserDto updateMyProfile(User user, UpdateUserRequestDto requestDto);
}
