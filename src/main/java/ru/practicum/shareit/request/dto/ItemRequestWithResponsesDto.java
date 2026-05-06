package ru.practicum.shareit.request.dto;

import lombok.Data;
import ru.practicum.shareit.item.dto.ItemRequestResponseDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ItemRequestWithResponsesDto {

    private Long id;
    private String description;
    private LocalDateTime created;
    private List<ItemRequestResponseDto> items = new ArrayList<>();
}
