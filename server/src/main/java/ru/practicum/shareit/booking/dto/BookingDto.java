package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.dto.UserIdDto;

import java.time.LocalDateTime;

public record BookingDto(
        Long id,
        LocalDateTime start,
        LocalDateTime end,
        UserIdDto booker,
        ItemShortDto item,
        Status status
) {
}
