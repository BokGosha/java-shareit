package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    List<ItemDto> getItems();

    ItemMoreDto getItemById(Long id);

    ItemDto createItem(Long ownerId, ItemCreateDto itemCreateDto);

    ItemDto updateItem(Long id, Long ownerId, ItemUpdateDto itemUpdateDto);

    List<ItemMoreDto> getItemsByOwnerId(Long ownerId);

    List<ItemDto> getItemsByText(String text);

    Item existsById(Long id);

    CommentDto createComment(Long bookerId, Long itemId, CommentCreateDto itemCreateDto);
}
