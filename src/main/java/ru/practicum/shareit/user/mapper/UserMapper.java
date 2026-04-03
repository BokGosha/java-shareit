package ru.practicum.shareit.user.mapper;

import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;

public final class UserMapper {

    public static UserDto mapUserToUserDto(User user) {
        UserDto userDto = new UserDto();

        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());

        return userDto;
    }

    public static User mapUserCreateDtoToUser(UserCreateDto userCreateDto) {
        User user = new User();

        if (userCreateDto.getName() != null) {
            user.setName(userCreateDto.getName());
        }

        user.setEmail(userCreateDto.getEmail());

        return user;
    }

    public static User updateUserFields(User user, UserUpdateDto userUpdateDto) {
        if (userUpdateDto.getName() != null) {
            user.setName(userUpdateDto.getName());
        }

        if (userUpdateDto.getEmail() != null) {
            user.setEmail(userUpdateDto.getEmail());
        }

        return user;
    }
}
