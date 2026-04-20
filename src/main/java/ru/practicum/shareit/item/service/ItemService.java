package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    List<ItemDto> getItems();

    ItemDto getItemById(Long id);

    ItemDto createItem(Long ownerId, ItemCreateDto itemCreateDto);

    ItemDto updateItem(Long id, Long ownerId, ItemUpdateDto itemUpdateDto);

    List<ItemDto> getItemsByUserId(Long ownerId);

    List<ItemDto> getItemsByText(String text);

    Item existsById(Long id);
}
