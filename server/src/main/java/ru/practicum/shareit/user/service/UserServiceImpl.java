package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    @Override
    public List<UserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::mapUserToUserDto)
                .toList();
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = existsById(userId);

        return userMapper.mapUserToUserDto(user);
    }

    @Override
    @Transactional
    public UserDto createUser(UserCreateDto userCreateDto) {
        existsByEmail(userCreateDto.email());

        User user = userMapper.mapUserCreateDtoToUser(userCreateDto);

        user = userRepository.save(user);

        return userMapper.mapUserToUserDto(user);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long userId, UserUpdateDto userUpdateDto) {
        User user = existsById(userId);
        existsByEmail(userUpdateDto.email());

        userMapper.updateUserFields(user, userUpdateDto);

        return userMapper.mapUserToUserDto(user);
    }

    @Override
    @Transactional
    public void deleteUserById(Long userId) {
        existsById(userId);

        userRepository.deleteById(userId);
    }

    public User existsById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    private void existsByEmail(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException("Пользователь с почтой=" + email + " уже существует");
        }
    }
}
