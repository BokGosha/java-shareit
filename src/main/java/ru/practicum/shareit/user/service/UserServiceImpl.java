package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserDto> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::mapUserToUserDto)
                .toList();
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = existsById(id);

        return UserMapper.mapUserToUserDto(user);
    }

    @Override
    @Transactional
    public UserDto createUser(UserCreateDto userCreateDto) {
        existsByEmail(userCreateDto.getEmail());

        User user = UserMapper.mapUserCreateDtoToUser(userCreateDto);

        user = userRepository.save(user);

        return UserMapper.mapUserToUserDto(user);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserUpdateDto userUpdateDto) {
        User user = existsById(id);
        existsByEmail(userUpdateDto.getEmail());

        UserMapper.updateUserFields(user, userUpdateDto);

        return UserMapper.mapUserToUserDto(user);
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        existsById(id);

        userRepository.deleteById(id);
    }

    public User existsById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    private void existsByEmail(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException("Пользователь с почтой=" + email + " уже существует");
        }
    }
}
