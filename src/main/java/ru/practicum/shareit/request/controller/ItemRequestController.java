package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithResponsesDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService requestService;

    @GetMapping
    public List<ItemRequestWithResponsesDto> getUserRequests(@RequestHeader("X-Sharer-User-Id") Long requestorId) {
        return requestService.getUserRequests(requestorId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getUsersRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestService.getUsersRequests(userId);
    }

    @PostMapping
    public ItemRequestDto createRequest(
            @RequestHeader("X-Sharer-User-Id") Long requestorId,
            @Valid @RequestBody ItemRequestCreateDto itemRequestCreateDto
    ) {
        return requestService.createRequest(requestorId, itemRequestCreateDto);
    }

    @GetMapping("/{requestId}")
    public ItemRequestWithResponsesDto getRequestById(@PathVariable Long requestId) {
        return requestService.getRequestById(requestId);
    }
}
