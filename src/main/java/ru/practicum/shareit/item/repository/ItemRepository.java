package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {

    Optional<Item> findById(Long id);

    List<Item> findAll();

    Item save(Item item);

    Item update(Item item);

    List<Item> findAllByUserId(Long userId);

    List<Item> findAllByText(String text);
}
