package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record BookingCreateDto(
        @NotNull Long itemId,
        @NotNull LocalDateTime start,
        @NotNull LocalDateTime end
) {
}
