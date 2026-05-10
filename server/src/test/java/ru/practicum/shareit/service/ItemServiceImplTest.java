package ru.practicum.shareit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UserIsNotBookerException;
import ru.practicum.shareit.exception.UserIsNotOwnerException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMoreDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRequestService itemRequestService;
    @Mock
    private UserService userService;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User other;
    private Item item;
    private ItemDto itemDto;
    private ItemMoreDto itemMoreDto;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);

        other = new User();
        other.setId(2L);

        item = new Item();
        item.setId(10L);
        item.setName("Drill");
        item.setDescription("Powerful");
        item.setIsAvailable(true);
        item.setOwner(owner);

        itemDto = new ItemDto(10L, "Drill", "Powerful", true, List.of());
        itemMoreDto = new ItemMoreDto(10L, "Drill", "Powerful", true, null, null, List.of());
    }

    @Test
    @DisplayName("getItems: returns mapped list")
    void getItems_returnsList() {
        when(itemRepository.findAll()).thenReturn(List.of(item));
        when(itemMapper.mapItemToItemDto(item)).thenReturn(itemDto);

        List<ItemDto> result = itemService.getItems();

        assertThat(result).containsExactly(itemDto);
    }

    @Test
    @DisplayName("getItems: returns empty list")
    void getItems_emptyRepo_returnsEmpty() {
        when(itemRepository.findAll()).thenReturn(List.of());

        assertThat(itemService.getItems()).isEmpty();
    }

    @Test
    @DisplayName("getItemById: returns item with comments")
    void getItemById_whenExists_returnsItem() {
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItem_Id(10L)).thenReturn(List.of());
        when(itemMapper.mapItemToItemMoreDto(item)).thenReturn(itemMoreDto);

        ItemMoreDto result = itemService.getItemById(10L);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.comments()).isEmpty();
    }

    @Test
    @DisplayName("getItemById: throws when missing")
    void getItemById_whenMissing_throws() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getItemById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("createItem: saves item without request")
    void createItem_withoutRequest_saves() {
        ItemCreateDto dto = new ItemCreateDto("Drill", "Powerful", true, null);
        when(userService.existsById(1L)).thenReturn(owner);
        when(itemMapper.mapItemCreateDtoToItem(dto)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.mapItemToItemDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.createItem(1L, dto);

        assertThat(result).isEqualTo(itemDto);
        verify(itemRequestService, never()).existsById(any());
    }

    @Test
    @DisplayName("createItem: saves item with request")
    void createItem_withRequest_saves() {
        ItemCreateDto dto = new ItemCreateDto("Drill", "Powerful", true, 5L);
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(5L);

        when(userService.existsById(1L)).thenReturn(owner);
        when(itemMapper.mapItemCreateDtoToItem(dto)).thenReturn(item);
        when(itemRequestService.existsById(5L)).thenReturn(itemRequest);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.mapItemToItemDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.createItem(1L, dto);

        assertThat(result).isEqualTo(itemDto);
        assertThat(item.getRequest()).isEqualTo(itemRequest);
    }

    @Test
    @DisplayName("updateItem: updates when owner matches")
    void updateItem_whenOwner_updates() {
        ItemUpdateDto dto = new ItemUpdateDto("New", "Desc", false);
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(userService.existsById(1L)).thenReturn(owner);
        when(itemMapper.mapItemToItemDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.updateItem(10L, 1L, dto);

        assertThat(result).isEqualTo(itemDto);
        verify(itemMapper).updateItemFields(item, dto);
    }

    @Test
    @DisplayName("updateItem: throws when not owner")
    void updateItem_whenNotOwner_throws() {
        ItemUpdateDto dto = new ItemUpdateDto("New", "Desc", false);
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(userService.existsById(2L)).thenReturn(other);

        assertThatThrownBy(() -> itemService.updateItem(10L, 2L, dto))
                .isInstanceOf(UserIsNotOwnerException.class);
    }

    @Test
    @DisplayName("getItemsByOwnerId: returns items")
    void getItemsByOwnerId_whenItemsExist_returnsList() {
        when(userService.existsById(1L)).thenReturn(owner);
        when(itemRepository.findAllByOwner_Id(1L)).thenReturn(List.of(item));
        when(bookingRepository.findAllByItem_IdInAndStatus(List.of(10L), Status.APPROVED))
                .thenReturn(List.of());
        when(commentRepository.findAllByItem_IdIn(List.of(10L))).thenReturn(List.of());
        when(itemMapper.mapItemToItemMoreDto(item)).thenReturn(itemMoreDto);

        List<ItemMoreDto> result = itemService.getItemsByOwnerId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getItemsByOwnerId: returns empty for owner without items")
    void getItemsByOwnerId_whenNoItems_returnsEmpty() {
        when(userService.existsById(1L)).thenReturn(owner);
        when(itemRepository.findAllByOwner_Id(1L)).thenReturn(List.of());
        when(bookingRepository.findAllByItem_IdInAndStatus(List.of(), Status.APPROVED))
                .thenReturn(List.of());

        List<ItemMoreDto> result = itemService.getItemsByOwnerId(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getItemsByText: returns empty for empty text")
    void getItemsByText_whenEmpty_returnsEmpty() {
        List<ItemDto> result = itemService.getItemsByText("");

        assertThat(result).isEmpty();
        verify(itemRepository, never()).findAllByText(any());
    }

    @Test
    @DisplayName("getItemsByText: returns matching items")
    void getItemsByText_whenMatches_returnsList() {
        when(itemRepository.findAllByText("drill")).thenReturn(List.of(item));
        when(itemMapper.mapItemToItemDto(item)).thenReturn(itemDto);

        List<ItemDto> result = itemService.getItemsByText("drill");

        assertThat(result).containsExactly(itemDto);
    }

    @Test
    @DisplayName("existsById: returns item when found")
    void existsById_whenFound_returns() {
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        Item result = itemService.existsById(10L);

        assertThat(result).isSameAs(item);
    }

    @Test
    @DisplayName("existsById: throws when missing")
    void existsById_whenMissing_throws() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.existsById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("createComment: throws when user has no bookings of the item")
    void createComment_whenNoBookings_throws() {
        CommentCreateDto dto = new CommentCreateDto("Nice");
        when(userService.existsById(2L)).thenReturn(other);
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByItem_IdAndBooker_IdAndStatus(10L, 2L, Status.APPROVED))
                .thenReturn(List.of());

        assertThatThrownBy(() -> itemService.createComment(2L, 10L, dto))
                .isInstanceOf(UserIsNotBookerException.class);
    }

    @Test
    @DisplayName("createComment: saves comment when booking finished")
    void createComment_whenBookingFinished_saves() {
        CommentCreateDto dto = new CommentCreateDto("Nice");
        Booking booking = new Booking();
        booking.setEnd(LocalDateTime.now().minusDays(1));

        Comment comment = new Comment();
        CommentDto commentDto = new CommentDto(1L, "Nice", "Other", LocalDateTime.now());

        when(userService.existsById(2L)).thenReturn(other);
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByItem_IdAndBooker_IdAndStatus(10L, 2L, Status.APPROVED))
                .thenReturn(List.of(booking));
        when(commentMapper.mapCommentCreateDtoToComment(dto)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentMapper.mapCommentToCommentDto(comment)).thenReturn(commentDto);

        CommentDto result = itemService.createComment(2L, 10L, dto);

        assertThat(result).isEqualTo(commentDto);
        assertThat(comment.getAuthor()).isEqualTo(other);
        assertThat(comment.getItem()).isEqualTo(item);
    }

    @Test
    @DisplayName("createComment: throws when booking still active")
    void createComment_whenBookingActive_throws() {
        CommentCreateDto dto = new CommentCreateDto("Nice");
        Booking booking = new Booking();
        booking.setEnd(LocalDateTime.now().plusDays(1));

        when(userService.existsById(2L)).thenReturn(other);
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByItem_IdAndBooker_IdAndStatus(10L, 2L, Status.APPROVED))
                .thenReturn(List.of(booking));

        assertThatThrownBy(() -> itemService.createComment(2L, 10L, dto))
                .isInstanceOf(BadRequestException.class);
    }
}
