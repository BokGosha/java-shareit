package ru.practicum.shareit.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRepositoryTest {

    private final TestEntityManager em;

    private final ItemRepository itemRepository;

    private User owner;
    private User otherOwner;
    private ItemRequest request;

    @BeforeEach
    public void setUp() {
        owner = newUser("Owner", "owner@example.com");
        otherOwner = newUser("Other", "other@example.com");
        request = newRequest(owner, "Need a drill");
    }

    @Test
    @DisplayName("findAllByText: matches by name and description, case-insensitive")
    public void findAllByText_matchesByNameAndDescription() {
        newItem("Drill", "Powerful tool", true, owner, null);
        newItem("Saw", "Hand drill description", true, owner, null);
        newItem("Hammer", "Wooden handle", true, owner, null);
        em.flush();

        List<Item> result = itemRepository.findAllByText("DRILL");

        assertThat(result).extracting(Item::getName)
                .containsExactlyInAnyOrder("Drill", "Saw");
    }

    @Test
    @DisplayName("findAllByText: empty result when no match")
    public void findAllByText_noMatch_returnsEmpty() {
        newItem("Drill", "tool", true, owner, null);
        em.flush();

        List<Item> result = itemRepository.findAllByText("xyz");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAllByOwner_Id: returns only owner items")
    public void findAllByOwnerId_returnsOnlyOwnerItems() {
        newItem("Drill", "tool", true, owner, null);
        newItem("Saw", "tool", true, owner, null);
        newItem("Hammer", "tool", true, otherOwner, null);
        em.flush();

        List<Item> result = itemRepository.findAllByOwner_Id(owner.getId());

        assertThat(result).hasSize(2)
                .extracting(Item::getName)
                .containsExactlyInAnyOrder("Drill", "Saw");
    }

    @Test
    @DisplayName("findAllByRequest_Id: returns items linked to the given request")
    public void findAllByRequestId_returnsItemsForRequest() {
        newItem("Drill", "tool", true, owner, request);
        newItem("Saw", "tool", true, otherOwner, null);
        em.flush();

        List<Item> result = itemRepository.findAllByRequest_Id(request.getId());

        assertThat(result).hasSize(1)
                .extracting(Item::getName).containsExactly("Drill");
    }

    @Test
    @DisplayName("findAllByRequest_IdIn: returns items for any of the given request ids")
    public void findAllByRequestIdIn_returnsItems() {
        ItemRequest second = newRequest(otherOwner, "Need a saw");
        newItem("Drill", "tool", true, owner, request);
        newItem("Saw", "tool", true, otherOwner, second);
        newItem("Hammer", "tool", true, owner, null);
        em.flush();

        List<Item> result = itemRepository.findAllByRequest_IdIn(List.of(request.getId(), second.getId()));

        assertThat(result).hasSize(2)
                .extracting(Item::getName)
                .containsExactlyInAnyOrder("Drill", "Saw");
    }

    private User newUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return em.persist(user);
    }

    private ItemRequest newRequest(User requestor, String description) {
        ItemRequest r = new ItemRequest();
        r.setRequestor(requestor);
        r.setDescription(description);
        r.setCreated(LocalDateTime.now());
        return em.persist(r);
    }

    private Item newItem(String name, String description, boolean available, User owner, ItemRequest request) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setIsAvailable(available);
        item.setOwner(owner);
        item.setRequest(request);
        return em.persist(item);
    }
}
