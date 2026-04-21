package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemMapper {

    public static ItemDto mapItemToItemDto(Item item) {
        ItemDto itemDto = new ItemDto();

        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getIsAvailable());

        return itemDto;
    }

    public static ItemMoreDto mapItemToItemMoreDto(Item item) {
        ItemMoreDto itemMoreDto = new ItemMoreDto();

        itemMoreDto.setId(item.getId());
        itemMoreDto.setName(item.getName());
        itemMoreDto.setDescription(item.getDescription());
        itemMoreDto.setAvailable(item.getIsAvailable());

        return itemMoreDto;
    }

    public static Item mapItemCreateDtoToItem(ItemCreateDto itemCreateDto) {
        Item item = new Item();

        item.setName(itemCreateDto.getName());
        item.setDescription(itemCreateDto.getDescription());
        item.setIsAvailable(itemCreateDto.getAvailable());

        return item;
    }

    public static ItemShortDto mapItemToItemShortDto(Item item) {
        ItemShortDto itemShortDto = new ItemShortDto();

        itemShortDto.setId(item.getId());
        itemShortDto.setName(item.getName());

        return itemShortDto;
    }

    public static Item updateItemFields(Item item, ItemUpdateDto itemUpdateDto) {
        if (itemUpdateDto.getName() != null) {
            item.setName(itemUpdateDto.getName());
        }

        if (itemUpdateDto.getDescription() != null) {
            item.setDescription(itemUpdateDto.getDescription());
        }

        if (itemUpdateDto.getAvailable() != null) {
            item.setIsAvailable(itemUpdateDto.getAvailable());
        }

        return item;
    }
}
