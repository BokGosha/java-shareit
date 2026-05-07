package ru.practicum.shareit.item.dto;

import java.util.List;

public record ItemDto(
        Long id,
        String name,
        String description,
        Boolean available,
        List<CommentDto> comments
) {
    public ItemDto {
        comments = comments != null ? comments : List.of();
    }
}
