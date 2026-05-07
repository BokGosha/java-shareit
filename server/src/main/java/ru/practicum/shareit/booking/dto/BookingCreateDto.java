package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

public record BookingCreateDto(
        Long itemId,
        LocalDateTime start,
        LocalDateTime end
) {
}
