package ru.practicum.shareit.item.dto;

public record ItemUpdateDto(
        String name,
        String description,
        Boolean available
) {
}
