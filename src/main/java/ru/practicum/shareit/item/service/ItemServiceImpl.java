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

    private final UserService userService;

    @Override
    public List<ItemDto> getItems() {
        return itemRepository.findAll()
                .stream()
                .map(ItemMapper::mapItemToItemDto)
                .toList();
    }

    @Override
    public ItemMoreDto getItemById(Long id) {
        Item item = existsById(id);

        List<CommentDto> itemComments = getCommentsByItemId(id);
//        List<BookingShortDto> itemBookings = bookingRepository.findAllByItem_Id(id)
//                .stream()
//                .map(BookingMapper::mapBookingToBookingShortDto)
//                .toList();

        ItemMoreDto itemMoreDto = ItemMapper.mapItemToItemMoreDto(item);
        itemMoreDto.setComments(itemComments);

//        LocalDateTime now = LocalDateTime.now();
//        setLastAndNextBooking(now, itemMoreDto, itemBookings);

        return itemMoreDto;
    }

    @Override
    @Transactional
    public ItemDto createItem(Long ownerId, ItemCreateDto itemCreateDto) {
        User user = userService.existsById(ownerId);
        Item item = ItemMapper.mapItemCreateDtoToItem(itemCreateDto);

        item.setOwner(user);

        item = itemRepository.save(item);

        return ItemMapper.mapItemToItemDto(item);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long id, Long ownerId, ItemUpdateDto itemUpdateDto) {
        Item item = existsById(id);
        User user = userService.existsById(ownerId);

        if (!item.getOwner().getId().equals(user.getId())) {
            throw new UserIsNotOwnerException("Пользователь с id=" + ownerId + " не является владельцем вещи с id=" + id);
        }

        ItemMapper.updateItemFields(item, itemUpdateDto);

        return ItemMapper.mapItemToItemDto(item);
    }

    @Override
    public List<ItemMoreDto> getItemsByOwnerId(Long ownerId) {
        userService.existsById(ownerId);

        List<Item> items = itemRepository.findAllByOwner_Id(ownerId);
        List<Long> itemIds = items.stream().map(Item::getId).toList();

        Map<Long, List<BookingShortDto>> bookingsByItem =
                bookingRepository.findAllByItem_IdInAndStatus(itemIds, Status.APPROVED)
                        .stream()
                        .map(BookingMapper::mapBookingToBookingShortDto)
                        .collect(Collectors.groupingBy(BookingShortDto::getItemId));

        Map<Long, List<CommentDto>> commentsByItem =
                getCommentsByItemIds(itemIds);

        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    ItemMoreDto itemMoreDto = ItemMapper.mapItemToItemMoreDto(item);
                    List<BookingShortDto> itemBookings =
                            bookingsByItem.getOrDefault(item.getId(), List.of());

                    setLastAndNextBooking(now, itemMoreDto, itemBookings);

                    List<CommentDto> itemComments =
                            commentsByItem.getOrDefault(item.getId(), List.of());
                    itemMoreDto.setComments(itemComments);

                    return itemMoreDto;
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
                .map(ItemMapper::mapItemToItemDto)
                .toList();
    }

    @Override
    public Item existsById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + id + " не найдена"));
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

        Comment comment = CommentMapper.mapCommentCreateDtoToComment(commentCreateDto);

        comment.setCreated(LocalDateTime.now());
        comment.setAuthor(booker);
        comment.setItem(item);

        comment = commentRepository.save(comment);

        return CommentMapper.mapCommentToCommentDto(comment);
    }

    private Map<Long, List<CommentDto>> getCommentsByItemIds(List<Long> itemIds) {
        if (itemIds.isEmpty()) {
            return Map.of();
        }

        return commentRepository
                .findAllByItem_IdIn(itemIds)
                .stream()
                .map(CommentMapper::mapCommentToCommentDto)
                .collect(Collectors.groupingBy(CommentDto::getItemId));
    }

    private void setLastAndNextBooking(LocalDateTime now, ItemMoreDto itemMoreDto, List<BookingShortDto> itemBookings) {
        itemMoreDto.setLastBooking(itemBookings.stream()
                .filter(b -> b.getStart().isBefore(now))
                .max(Comparator.comparing(BookingShortDto::getStart))
                .orElse(null));

        itemMoreDto.setNextBooking(itemBookings.stream()
                .filter(b -> b.getStart().isAfter(now))
                .min(Comparator.comparing(BookingShortDto::getStart))
                .orElse(null));
    }

    private List<CommentDto> getCommentsByItemId(Long itemId) {
        return commentRepository.findAllByItem_Id(itemId)
                .stream()
                .map(CommentMapper::mapCommentToCommentDto)
                .toList();
    }
}
