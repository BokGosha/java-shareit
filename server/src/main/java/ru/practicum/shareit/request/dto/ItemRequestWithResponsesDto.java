package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.item.dto.ItemRequestResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public record ItemRequestWithResponsesDto(
        Long id,
        String description,
        LocalDateTime created,
        List<ItemRequestResponseDto> items
) {
    public ItemRequestWithResponsesDto {
        items = items != null ? items : List.of();
    }
}
