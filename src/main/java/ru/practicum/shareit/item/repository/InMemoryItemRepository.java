package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Repository
public class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, Item> items = new HashMap<>();

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public Item save(Item item) {
        item.setId(getNextId());
        items.put(item.getId(), item);

        return item;
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);

        return item;
    }

    @Override
    public List<Item> findAllByUserId(Long userId) {
        return items.values().stream()
                .filter(item -> Objects.equals(item.getOwner().getId(), userId))
                .toList();
    }

    @Override
    public List<Item> findAllByText(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }

        return items.values().stream()
                .filter(item -> ((item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                        && item.getAvailable() == true)
                )
                .toList();
    }

    private long getNextId() {
        long currentId = items.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++currentId;
    }
}
