package com.marek.carsharing.mapper;

import com.marek.carsharing.config.MapperConfig;
import com.marek.carsharing.dto.user.UpdateUserRequestDto;
import com.marek.carsharing.dto.user.UserDto;
import com.marek.carsharing.dto.user.registration.RegisterRequestDto;
import com.marek.carsharing.model.classes.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface UserMapper {

    UserDto toDto(User user);

    User toEntity(UpdateUserRequestDto userDto);

    @Mapping(target = "email", source = "email")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    User toEntity(RegisterRequestDto requestDto);
}

