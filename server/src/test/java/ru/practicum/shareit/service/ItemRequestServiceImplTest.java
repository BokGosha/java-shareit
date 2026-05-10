package ru.practicum.shareit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemRequestResponseDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithResponsesDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserService userService;
    @Mock
    private ItemRequestMapper itemRequestMapper;
    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User requestor;
    private ItemRequest itemRequest;
    private ItemRequestDto itemRequestDto;
    private ItemRequestWithResponsesDto withResponsesDto;

    @BeforeEach
    void setUp() {
        requestor = new User();
        requestor.setId(1L);

        itemRequest = new ItemRequest();
        itemRequest.setId(5L);
        itemRequest.setDescription("Need drill");
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());

        itemRequestDto = new ItemRequestDto(5L, "Need drill", 1L, itemRequest.getCreated());
        withResponsesDto = new ItemRequestWithResponsesDto(5L, "Need drill", itemRequest.getCreated(), List.of());
    }

    @Test
    @DisplayName("getUserRequests: returns requests with responses")
    void getUserRequests_returnsList() {
        Item item = new Item();
        item.setId(10L);
        item.setRequest(itemRequest);

        ItemRequestResponseDto responseDto = new ItemRequestResponseDto(10L, "Drill", 2L);

        when(userService.existsById(1L)).thenReturn(requestor);
        when(itemRequestRepository.findAllByRequestor_IdOrderByCreatedDesc(1L))
                .thenReturn(List.of(itemRequest));
        when(itemRepository.findAllByRequest_IdIn(List.of(5L))).thenReturn(List.of(item));
        when(itemMapper.mapItemToItemRequestResponseDto(item)).thenReturn(responseDto);
        when(itemRequestMapper.mapItemRequestToItemRequestWithResponsesDto(any(), anyList()))
                .thenReturn(withResponsesDto);

        List<ItemRequestWithResponsesDto> result = itemRequestService.getUserRequests(1L);

        assertThat(result).containsExactly(withResponsesDto);
    }

    @Test
    @DisplayName("getUserRequests: returns empty when user has no requests")
    void getUserRequests_whenNone_returnsEmpty() {
        when(userService.existsById(1L)).thenReturn(requestor);
        when(itemRequestRepository.findAllByRequestor_IdOrderByCreatedDesc(1L))
                .thenReturn(List.of());
        when(itemRepository.findAllByRequest_IdIn(List.of())).thenReturn(List.of());

        List<ItemRequestWithResponsesDto> result = itemRequestService.getUserRequests(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getUsersRequests: returns other users' requests")
    void getUsersRequests_returnsList() {
        when(userService.existsById(1L)).thenReturn(requestor);
        when(itemRequestRepository.findAllOByIdIsNotOrderByCreatedDesc(1L))
                .thenReturn(List.of(itemRequest));
        when(itemRequestMapper.mapItemRequestToItemRequestDto(itemRequest)).thenReturn(itemRequestDto);

        List<ItemRequestDto> result = itemRequestService.getUsersRequests(1L);

        assertThat(result).containsExactly(itemRequestDto);
    }

    @Test
    @DisplayName("getUsersRequests: returns empty when no other requests")
    void getUsersRequests_whenNone_returnsEmpty() {
        when(userService.existsById(1L)).thenReturn(requestor);
        when(itemRequestRepository.findAllOByIdIsNotOrderByCreatedDesc(1L)).thenReturn(List.of());

        assertThat(itemRequestService.getUsersRequests(1L)).isEmpty();
    }

    @Test
    @DisplayName("createRequest: saves and returns dto")
    void createRequest_saves() {
        ItemRequestCreateDto createDto = new ItemRequestCreateDto("Need drill");

        when(userService.existsById(1L)).thenReturn(requestor);
        when(itemRequestMapper.mapItemRequestCreateDtoToItem(createDto)).thenReturn(itemRequest);
        when(itemRequestRepository.save(itemRequest)).thenReturn(itemRequest);
        when(itemRequestMapper.mapItemRequestToItemRequestDto(itemRequest)).thenReturn(itemRequestDto);

        ItemRequestDto result = itemRequestService.createRequest(1L, createDto);

        assertThat(result).isEqualTo(itemRequestDto);
        assertThat(itemRequest.getRequestor()).isEqualTo(requestor);
        assertThat(itemRequest.getCreated()).isNotNull();
        verify(itemRequestRepository).save(itemRequest);
    }

    @Test
    @DisplayName("createRequest: throws when user not found")
    void createRequest_whenUserMissing_throws() {
        ItemRequestCreateDto createDto = new ItemRequestCreateDto("Need drill");
        when(userService.existsById(999L)).thenThrow(new NotFoundException("not found"));

        assertThatThrownBy(() -> itemRequestService.createRequest(999L, createDto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("getRequestById: returns request with responses")
    void getRequestById_whenFound_returns() {
        Item item = new Item();
        item.setId(10L);
        ItemRequestResponseDto responseDto = new ItemRequestResponseDto(10L, "Drill", 2L);

        when(itemRequestRepository.findById(5L)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findAllByRequest_Id(5L)).thenReturn(List.of(item));
        when(itemMapper.mapItemToItemRequestResponseDto(item)).thenReturn(responseDto);
        when(itemRequestMapper.mapItemRequestToItemRequestWithResponsesDto(itemRequest, List.of(responseDto)))
                .thenReturn(withResponsesDto);

        ItemRequestWithResponsesDto result = itemRequestService.getRequestById(5L);

        assertThat(result).isEqualTo(withResponsesDto);
    }

    @Test
    @DisplayName("getRequestById: throws when missing")
    void getRequestById_whenMissing_throws() {
        when(itemRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemRequestService.getRequestById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("existsById: returns request when found")
    void existsById_whenFound_returns() {
        when(itemRequestRepository.findById(5L)).thenReturn(Optional.of(itemRequest));

        assertThat(itemRequestService.existsById(5L)).isSameAs(itemRequest);
    }

    @Test
    @DisplayName("existsById: throws when missing")
    void existsById_whenMissing_throws() {
        when(itemRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemRequestService.existsById(99L))
                .isInstanceOf(NotFoundException.class);
    }
}
