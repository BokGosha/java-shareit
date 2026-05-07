package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateDto(
        @NotBlank String text
) {
}
