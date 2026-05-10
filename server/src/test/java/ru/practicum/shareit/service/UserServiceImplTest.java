package ru.practicum.shareit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Name");
        user.setEmail("user@example.com");

        userDto = new UserDto(1L, "Name", "user@example.com");
    }

    @Test
    @DisplayName("getUsers: returns mapped list")
    void getUsers_whenUsersExist_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.mapUserToUserDto(user)).thenReturn(userDto);

        List<UserDto> result = userService.getUsers();

        assertThat(result).containsExactly(userDto);
    }

    @Test
    @DisplayName("getUsers: returns empty list")
    void getUsers_whenNoUsers_returnsEmpty() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserDto> result = userService.getUsers();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getUserById: returns user when exists")
    void getUserById_whenExists_returnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.mapUserToUserDto(user)).thenReturn(userDto);

        UserDto result = userService.getUserById(1L);

        assertThat(result).isEqualTo(userDto);
    }

    @Test
    @DisplayName("getUserById: throws NotFoundException when missing")
    void getUserById_whenMissing_throws() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("createUser: saves and returns dto")
    void createUser_whenEmailFree_savesUser() {
        UserCreateDto createDto = new UserCreateDto("Name", "user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        when(userMapper.mapUserCreateDtoToUser(createDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.mapUserToUserDto(user)).thenReturn(userDto);

        UserDto result = userService.createUser(createDto);

        assertThat(result).isEqualTo(userDto);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("createUser: throws when email already exists")
    void createUser_whenEmailTaken_throws() {
        UserCreateDto createDto = new UserCreateDto("Name", "user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.createUser(createDto))
                .isInstanceOf(EmailAlreadyExistsException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUser: updates fields")
    void updateUser_whenExists_updates() {
        UserUpdateDto updateDto = new UserUpdateDto("New", "new@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userMapper.mapUserToUserDto(user)).thenReturn(userDto);

        UserDto result = userService.updateUser(1L, updateDto);

        assertThat(result).isEqualTo(userDto);
        verify(userMapper).updateUserFields(user, updateDto);
    }

    @Test
    @DisplayName("updateUser: throws when user not found")
    void updateUser_whenMissing_throws() {
        UserUpdateDto updateDto = new UserUpdateDto("New", "new@example.com");
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(999L, updateDto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("updateUser: throws when new email already taken")
    void updateUser_whenEmailTaken_throws() {
        UserUpdateDto updateDto = new UserUpdateDto("New", "taken@example.com");
        User other = new User();
        other.setId(2L);
        other.setEmail("taken@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> userService.updateUser(1L, updateDto))
                .isInstanceOf(EmailAlreadyExistsException.class);
        verify(userMapper, never()).updateUserFields(any(), any());
    }

    @Test
    @DisplayName("deleteUserById: deletes when exists")
    void deleteUserById_whenExists_deletes() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUserById(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteUserById: throws when missing")
    void deleteUserById_whenMissing_throws() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUserById(999L))
                .isInstanceOf(NotFoundException.class);
        verify(userRepository, never()).deleteById(any());
    }
}
