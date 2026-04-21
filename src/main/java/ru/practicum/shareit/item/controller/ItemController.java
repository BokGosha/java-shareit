package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
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
    public ItemMoreDto getItemById(@PathVariable Long id) {
        return itemService.getItemById(id);
    }

    @GetMapping(headers = "X-Sharer-User-Id")
    public List<ItemMoreDto> getItemsByOwnerId(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.getItemsByOwnerId(ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> getItemsByUserId(@RequestParam String text) {
        return itemService.getItemsByText(text);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                              @Valid @RequestBody ItemCreateDto itemCreateDto) {
        return itemService.createItem(ownerId, itemCreateDto);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@RequestHeader("X-Sharer-User-Id") Long bookerId,
                                    @PathVariable Long itemId,
                                    @Valid @RequestBody CommentCreateDto itemCreateDto) {
        return itemService.createComment(bookerId, itemId, itemCreateDto);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@PathVariable Long id,
                              @RequestHeader("X-Sharer-User-Id") Long ownerId,
                              @RequestBody ItemUpdateDto itemUpdateDto) {
        return itemService.updateItem(id, ownerId, itemUpdateDto);
    }
}
