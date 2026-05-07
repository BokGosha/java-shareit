package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {

    List<UserDto> getUsers();

    UserDto getUserById(Long userId);

    UserDto createUser(UserCreateDto userCreateDto);

    UserDto updateUser(Long userId, UserUpdateDto userUpdateDto);

    void deleteUserById(Long userId);

    User existsById(Long userId);
}
