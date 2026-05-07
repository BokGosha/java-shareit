package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemRequestResponseDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithResponsesDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;

    private final UserService userService;

    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;

    @Override
    public List<ItemRequestWithResponsesDto> getUserRequests(Long requestorId) {
        userService.existsById(requestorId);

        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequestor_IdOrderByCreatedDesc(requestorId);
        List<Long> itemRequestIds = itemRequests.stream().map(ItemRequest::getId).toList();

        Map<Long, List<ItemRequestResponseDto>> responses = itemRepository.findAllByRequest_IdIn(itemRequestIds).stream().collect(Collectors.groupingBy(item -> item.getRequest().getId(), Collectors.mapping(itemMapper::mapItemToItemRequestResponseDto, Collectors.toList())));

        return itemRequests.stream()
                .map(itemRequest -> {
                    List<ItemRequestResponseDto> itemResponses = responses.getOrDefault(itemRequest.getId(), List.of());

                    return itemRequestMapper.mapItemRequestToItemRequestWithResponsesDto(itemRequest, itemResponses);
                })
                .toList();
    }

    @Override
    public List<ItemRequestDto> getUsersRequests(Long userId) {
        userService.existsById(userId);

        return itemRequestRepository.findAllOByIdIsNotOrderByCreatedDesc(userId).stream()
                .map(itemRequestMapper::mapItemRequestToItemRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public ItemRequestDto createRequest(Long requestorId, ItemRequestCreateDto itemRequestCreateDto) {
        User requestor = userService.existsById(requestorId);

        ItemRequest itemRequest = itemRequestMapper.mapItemRequestCreateDtoToItem(itemRequestCreateDto);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setRequestor(requestor);

        itemRequest = itemRequestRepository.save(itemRequest);

        return itemRequestMapper.mapItemRequestToItemRequestDto(itemRequest);
    }

    @Override
    public ItemRequestWithResponsesDto getRequestById(Long requestId) {
        ItemRequest itemRequest = existsById(requestId);

        List<ItemRequestResponseDto> itemResponses = itemRepository.findAllByRequest_Id(requestId).stream()
                .map(itemMapper::mapItemToItemRequestResponseDto)
                .toList();

        return itemRequestMapper.mapItemRequestToItemRequestWithResponsesDto(itemRequest, itemResponses);
    }

    @Override
    public ItemRequest existsById(Long requestId) {
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id=" + requestId + " не найден"));
    }
}
