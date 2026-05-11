package ru.practicum.shareit.item.dto;

public record ItemRequestResponseDto(
        Long id,
        String name,
        Long ownerId
) {
}
