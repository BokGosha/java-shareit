package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithResponsesDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestService {

    List<ItemRequestWithResponsesDto> getUserRequests(Long requestorId);

    List<ItemRequestDto> getUsersRequests(Long userId);

    ItemRequestDto createRequest(Long requestorId, ItemRequestCreateDto itemRequestCreateDto);

    ItemRequestWithResponsesDto getRequestById(Long requestId);

    ItemRequest existsById(Long requestId);
}
