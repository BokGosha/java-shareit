package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping(path = "/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public List<ItemDto> getItems() {
        return itemService.getItems();
    }

    @GetMapping("/{id}")
    public ItemDto getItemById(@PathVariable Long id) {
        return itemService.getItemById(id);
    }

    @GetMapping(headers = "X-Sharer-User-Id")
    public List<ItemDto> getItemsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getItemsByUserId(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> getItemsByUserId(@RequestParam String text) {
        return itemService.getItemsByText(text);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto createItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody ItemCreateDto itemCreateDto
    ) {
        return itemService.createItem(userId, itemCreateDto);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(
            @PathVariable Long id,
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody ItemUpdateDto itemUpdateDto
    ) {
        return itemService.updateItem(id, userId, itemUpdateDto);
    }
}
