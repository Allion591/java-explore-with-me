package ru.practicum.main.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.main.dto.user.NewUserRequest;
import ru.practicum.main.dto.user.UserDto;
import ru.practicum.main.dto.user.UserShortDto;
import ru.practicum.main.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toUserDto(User user);

    UserShortDto toUserShortDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "requests", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    User toUser(NewUserRequest newUserRequest);
}