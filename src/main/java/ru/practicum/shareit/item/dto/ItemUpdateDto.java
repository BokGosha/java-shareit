package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ItemUpdateDto {

    @NotEmpty
    private String name;

    @NotEmpty
    private String description;

    private Boolean available;
}
