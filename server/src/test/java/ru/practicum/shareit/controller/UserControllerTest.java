package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserControllerTest {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDto userDto;

    @BeforeEach
    public void setUp() {
        userDto = new UserDto(1L, "Name", "example@example.com");
    }

    @Test
    @DisplayName("GET /users")
    public void getUsers_shouldReturnListOfUsers() throws Exception {
        UserDto second = new UserDto(2L, "Name2", "example2@example.com");
        when(userService.getUsers()).thenReturn(List.of(userDto, second));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Name"))
                .andExpect(jsonPath("$[0].email").value("example@example.com"))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(userService).getUsers();
    }

    @Test
    @DisplayName("GET /users/{userId}, user exists")
    public void getUserById_whenExists_shouldReturnUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userDto);

        mockMvc.perform(get("/users/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.email").value("example@example.com"));

        verify(userService).getUserById(1L);
    }

    @Test
    @DisplayName("GET /users/{userId}, user does not exist")
    public void getUserById_whenNotFound_shouldReturn404() throws Exception {
        when(userService.getUserById(999L))
                .thenThrow(new NotFoundException("Пользователь с id=999 не найден"));

        mockMvc.perform(get("/users/{userId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /users")
    public void createUser_shouldReturn201AndCreateUser() throws Exception {
        UserCreateDto request = new UserCreateDto("Name", "example@example.com");
        when(userService.createUser(any(UserCreateDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.email").value("example@example.com"));

        verify(userService).createUser(any(UserCreateDto.class));
    }

    @Test
    @DisplayName("POST /users, email is empty")
    public void createUser_whenEmailIsEmpty_shouldReturn400() throws Exception {
        UserCreateDto request = new UserCreateDto("Name", null);
        when(userService.createUser(any(UserCreateDto.class)))
                .thenThrow(new BadRequestException("email не может быть null"));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService).createUser(any(UserCreateDto.class));
    }

    @Test
    @DisplayName("PATCH /users")
    void updateUser_shouldReturnUpdatedUser() throws Exception {
        UserUpdateDto request = new UserUpdateDto("Name Updated", "new@example.com");
        UserDto updated = new UserDto(1L, "Name Updated", "new@example.com");

        when(userService.updateUser(eq(1L), any(UserUpdateDto.class))).thenReturn(updated);

        mockMvc.perform(patch("/users/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Name Updated"))
                .andExpect(jsonPath("$.email").value("new@example.com"));

        verify(userService).updateUser(eq(1L), any(UserUpdateDto.class));
    }

    @Test
    void updateUser_whenUserNotFound_shouldReturn404() throws Exception {
        UserUpdateDto request = new UserUpdateDto("X", "x@example.com");
        when(userService.updateUser(eq(999L), any(UserUpdateDto.class)))
                .thenThrow(new NotFoundException("User 999 not found"));

        mockMvc.perform(patch("/users/{userId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_withPartialPayload_shouldStillWork() throws Exception {
        String partialJson = "{\"email\":\"new@example.com\"}";
        UserDto updated = new UserDto(1L, "Name Updated", "new@example.com");

        when(userService.updateUser(eq(1L), any(UserUpdateDto.class))).thenReturn(updated);

        mockMvc.perform(patch("/users/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(partialJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }

    @Test
    void deleteUser_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/users/{userId}", 1L))
                .andExpect(status().isNoContent());

        verify(userService).deleteUserById(1L);
    }

    @Test
    void deleteUser_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new NotFoundException("Пользователь с id=999 не найден"))
                .when(userService).deleteUserById(999L);

        mockMvc.perform(delete("/users/{userId}", 999L))
                .andExpect(status().isNotFound());
    }
}
