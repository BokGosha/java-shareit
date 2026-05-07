package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

@RestController
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {

    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object> getItemsByOwnerId(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Get items, ownerId={}", ownerId);
        return itemClient.getItemsByOwnerId(ownerId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable Long itemId) {
        log.info("Get item id={}", itemId);
        return itemClient.getItemById(itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam @NotBlank String text) {
        log.info("Search items text='{}'", text);
        return itemClient.searchItems(text);
    }

    @PostMapping
    public ResponseEntity<Object> createItem(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @Valid @RequestBody ItemCreateDto dto
    ) {
        log.info("Create item {}, ownerId={}", dto, ownerId);
        return itemClient.createItem(ownerId, dto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(
            @RequestHeader("X-Sharer-User-Id") Long bookerId,
            @PathVariable Long itemId,
            @Valid @RequestBody CommentCreateDto dto
    ) {
        log.info("Create comment for itemId={}, bookerId={}", itemId, bookerId);
        return itemClient.createComment(bookerId, itemId, dto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @RequestBody ItemUpdateDto dto
    ) {
        log.info("Update item id={}, ownerId={}, dto={}", itemId, ownerId, dto);
        return itemClient.updateItem(ownerId, itemId, dto);
    }
}
