package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UserIsNotBookerException;
import ru.practicum.shareit.exception.UserIsNotOwnerException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;

    private final ItemRequestService itemRequestService;
    private final UserService userService;

    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;
    private final BookingMapper bookingMapper;

    @Override
    public List<ItemDto> getItems() {
        return itemRepository.findAll()
                .stream()
                .map(itemMapper::mapItemToItemDto)
                .toList();
    }

    @Override
    public ItemMoreDto getItemById(Long itemId) {
        Item item = existsById(itemId);

        List<CommentDto> itemComments = getCommentsByItemId(itemId);
//        List<BookingShortDto> itemBookings = bookingRepository.findAllByItem_Id(itemId)
//                .stream()
//                .map(bookingMapper::mapBookingToBookingShortDto)
//                .toList();

        ItemMoreDto itemMoreDto = itemMapper.mapItemToItemMoreDto(item);

//        LocalDateTime now = LocalDateTime.now();
//        return setLastAndNextBooking(now, itemMoreDto, itemBookings, itemComments);

        return withComments(itemMoreDto, itemComments);
    }

    @Override
    @Transactional
    public ItemDto createItem(Long ownerId, ItemCreateDto itemCreateDto) {
        User user = userService.existsById(ownerId);
        Item item = itemMapper.mapItemCreateDtoToItem(itemCreateDto);

        if (itemCreateDto.requestId() != null) {
            ItemRequest itemRequest = itemRequestService.existsById(itemCreateDto.requestId());

            item.setRequest(itemRequest);
        }

        item.setOwner(user);

        item = itemRepository.save(item);

        return itemMapper.mapItemToItemDto(item);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long itemId, Long ownerId, ItemUpdateDto itemUpdateDto) {
        Item item = existsById(itemId);
        User user = userService.existsById(ownerId);

        if (!item.getOwner().getId().equals(user.getId())) {
            throw new UserIsNotOwnerException("Пользователь с id=" + ownerId + " не является владельцем вещи с id=" + itemId);
        }

        itemMapper.updateItemFields(item, itemUpdateDto);

        return itemMapper.mapItemToItemDto(item);
    }

    @Override
    public List<ItemMoreDto> getItemsByOwnerId(Long ownerId) {
        userService.existsById(ownerId);

        List<Item> items = itemRepository.findAllByOwner_Id(ownerId);
        List<Long> itemIds = items.stream().map(Item::getId).toList();

        Map<Long, List<BookingShortDto>> bookingsByItem =
                bookingRepository.findAllByItem_IdInAndStatus(itemIds, Status.APPROVED)
                        .stream()
                        .map(bookingMapper::mapBookingToBookingShortDto)
                        .collect(Collectors.groupingBy(BookingShortDto::itemId));

        Map<Long, List<CommentDto>> commentsByItem =
                getCommentsByItemIds(itemIds);

        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    ItemMoreDto itemMoreDto = itemMapper.mapItemToItemMoreDto(item);
                    List<BookingShortDto> itemBookings =
                            bookingsByItem.getOrDefault(item.getId(), List.of());

                    List<CommentDto> itemComments =
                            commentsByItem.getOrDefault(item.getId(), List.of());

                    return setLastAndNextBooking(now, itemMoreDto, itemBookings, itemComments);
                })
                .toList();
    }

    @Override
    public List<ItemDto> getItemsByText(String text) {
        if (text.isEmpty()) {
            return List.of();
        }

        List<Item> items = itemRepository.findAllByText(text);

        return items.stream()
                .map(itemMapper::mapItemToItemDto)
                .toList();
    }

    @Override
    public Item existsById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));
    }

    @Override
    @Transactional
    public CommentDto createComment(Long bookerId, Long itemId, CommentCreateDto commentCreateDto) {
        User booker = userService.existsById(bookerId);
        Item item = existsById(itemId);

        List<Booking> bookings = bookingRepository.findAllByItem_IdAndBooker_IdAndStatus(itemId, bookerId, Status.APPROVED);

        if (bookings.isEmpty()) {
            throw new UserIsNotBookerException("Пользовать с id=" + bookerId + " не является автором бронирования вещи");
        }

        if (bookings.stream().anyMatch(b -> b.getEnd().isAfter(LocalDateTime.now()))) {
            throw new BadRequestException("Отзыв пока нельзя оставить");
        }

        Comment comment = commentMapper.mapCommentCreateDtoToComment(commentCreateDto);

        comment.setCreated(LocalDateTime.now());
        comment.setAuthor(booker);
        comment.setItem(item);

        comment = commentRepository.save(comment);

        return commentMapper.mapCommentToCommentDto(comment);
    }

    private Map<Long, List<CommentDto>> getCommentsByItemIds(List<Long> itemIds) {
        if (itemIds.isEmpty()) {
            return Map.of();
        }

        return commentRepository
                .findAllByItem_IdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getItem().getId(),
                        Collectors.mapping(commentMapper::mapCommentToCommentDto, Collectors.toList())
                ));
    }

    private ItemMoreDto setLastAndNextBooking(LocalDateTime now,
                                              ItemMoreDto itemMoreDto,
                                              List<BookingShortDto> itemBookings,
                                              List<CommentDto> comments) {
        BookingShortDto lastBooking = itemBookings.stream()
                .filter(b -> b.start().isBefore(now))
                .max(Comparator.comparing(BookingShortDto::start))
                .orElse(null);

        BookingShortDto nextBooking = itemBookings.stream()
                .filter(b -> b.start().isAfter(now))
                .min(Comparator.comparing(BookingShortDto::start))
                .orElse(null);

        return new ItemMoreDto(
                itemMoreDto.id(),
                itemMoreDto.name(),
                itemMoreDto.description(),
                itemMoreDto.available(),
                lastBooking,
                nextBooking,
                comments
        );
    }

    private ItemMoreDto withComments(ItemMoreDto itemMoreDto, List<CommentDto> comments) {
        return new ItemMoreDto(
                itemMoreDto.id(),
                itemMoreDto.name(),
                itemMoreDto.description(),
                itemMoreDto.available(),
                itemMoreDto.lastBooking(),
                itemMoreDto.nextBooking(),
                comments
        );
    }

    private List<CommentDto> getCommentsByItemId(Long itemId) {
        return commentRepository.findAllByItem_Id(itemId)
                .stream()
                .map(commentMapper::mapCommentToCommentDto)
                .toList();
    }
}
