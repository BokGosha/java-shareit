package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.List;

public interface ItemService {

    List<ItemDto> getItems();

    ItemDto getItemById(Long id);

    ItemDto createItem(Long userId, ItemCreateDto itemCreateDto);

    ItemDto updateItem(Long id, Long userId, ItemUpdateDto itemUpdateDto);

    List<ItemDto> getItemsByUserId(Long userId);

    List<ItemDto> getItemsByText(String text);
}
