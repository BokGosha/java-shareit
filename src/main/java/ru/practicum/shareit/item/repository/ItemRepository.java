package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("""
        SELECT i
        FROM Item i
        WHERE UPPER(i.name) LIKE UPPER(CONCAT('%', ?1, '%'))
        OR UPPER(i.description) LIKE UPPER(CONCAT('%', ?1, '%'))
        AND i.isAvailable = true
    """)
    List<Item> findAllByText(String text);

    List<Item> findAllByOwner_Id(Long ownerId);

    List<Item> findAllByRequest_IdIn(List<Long> itemRequestIds);

    List<Item> findAllByRequest_Id(Long requestId);
}
