package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.service.BookingQueryService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UserIsNotOwnerException;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    private final UserService userService;
    private final BookingQueryService bookingQueryService;

    @Override
    public List<ItemDto> getItems() {
        return itemRepository.findAll()
                .stream()
                .map(ItemMapper::mapItemToItemDto)
                .toList();
    }

    @Override
    public ItemDto getItemById(Long id) {
        Item item = existsById(id);

        return ItemMapper.mapItemToItemDto(item);
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
    public List<ItemDto> getItemsByUserId(Long ownerId) {
        userService.existsById(ownerId);

        List<Item> items = itemRepository.findAllByOwner_Id(ownerId);
        List<Long> itemIds = items.stream().map(Item::getId).toList();

        Map<Long, List<BookingShortDto>> bookingsByItem =
                bookingQueryService.getApprovedBookingsByItemIds(itemIds);

        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    ItemDto itemDto = ItemMapper.mapItemToItemDto(item);
                    List<BookingShortDto> itemBookings =
                            bookingsByItem.getOrDefault(item.getId(), List.of());

                    itemDto.setLastBooking(itemBookings.stream()
                            .filter(b -> b.getStart().isBefore(now))
                            .max(Comparator.comparing(BookingShortDto::getStart))
                            .orElse(null));

                    itemDto.setNextBooking(itemBookings.stream()
                            .filter(b -> b.getStart().isAfter(now))
                            .min(Comparator.comparing(BookingShortDto::getStart))
                            .orElse(null));

                    return itemDto;
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

    public Item existsById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + id + " не найдена"));
    }
}
