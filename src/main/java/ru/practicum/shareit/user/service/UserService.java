package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {

    List<UserDto> getUsers();

    UserDto getUserById(Long id);

    UserDto createUser(UserCreateDto userCreateDto);

    UserDto updateUser(Long id, UserUpdateDto userUpdateDto);

    void deleteUserById(Long id);

    User existsById(Long id);
}
