package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UserIsNotOwnerException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public List<ItemDto> getItems() {
        return itemRepository.findAll().stream()
                .map(ItemMapper::mapItemToItemDto)
                .toList();
    }

    @Override
    public ItemDto getItemById(Long id) {
        Item item = existsById(id);

        return ItemMapper.mapItemToItemDto(item);
    }

    @Override
    public ItemDto createItem(Long userId, ItemCreateDto itemCreateDto) {
        User user = userService.existsById(userId);
        Item item = ItemMapper.mapItemCreateDtoToItem(itemCreateDto);

        item.setOwner(user);

        item = itemRepository.save(item);

        return ItemMapper.mapItemToItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long id, Long userId, ItemUpdateDto itemUpdateDto) {
        Item item = existsById(id);
        User user = userService.existsById(userId);

        if (item.getOwner().getId().equals(user.getId())) {
            item = ItemMapper.updateItemFields(item, itemUpdateDto);

            item = itemRepository.update(item);

            return ItemMapper.mapItemToItemDto(item);
        } else {
            throw new UserIsNotOwnerException("Пользователь с id=" + userId + " не является владельцем вещи с id=" + id);
        }
    }

    @Override
    public List<ItemDto> getItemsByUserId(Long userId) {
        userService.existsById(userId);

        List<Item> items = itemRepository.findAllByUserId(userId);

        return items.stream()
                .map(ItemMapper::mapItemToItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> getItemsByText(String text) {
        List<Item> items = itemRepository.findAllByText(text);

        return items.stream()
                .map(ItemMapper::mapItemToItemDto)
                .toList();
    }

    private Item existsById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }
}
