package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ItemCreateDto(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull Boolean available,
        Long requestId
) {
}
