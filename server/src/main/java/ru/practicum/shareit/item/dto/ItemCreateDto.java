package ru.practicum.shareit.item.dto;

public record ItemCreateDto(
        String name,
        String description,
        Boolean available,
        Long requestId
) {
}
