package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;

public record UserUpdateDto(
        String name,
        @Email String email
) {
}
