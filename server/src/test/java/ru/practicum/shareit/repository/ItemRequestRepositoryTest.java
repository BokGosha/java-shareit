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
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestRepositoryTest {

    private final TestEntityManager em;

    private final ItemRequestRepository requestRepository;

    private User user1;
    private User user2;

    @BeforeEach
    public void setUp() {
        user1 = newUser("U1", "u1@example.com");
        user2 = newUser("U2", "u2@example.com");
    }

    @Test
    @DisplayName("findAllByRequestor_IdOrderByCreatedDesc: returns user requests ordered desc")
    public void findAllByRequestorId_orderByCreatedDesc() {
        ItemRequest older = newRequest(user1, "old", LocalDateTime.now().minusDays(2));
        ItemRequest newer = newRequest(user1, "new", LocalDateTime.now().minusDays(1));
        newRequest(user2, "other", LocalDateTime.now());
        em.flush();

        List<ItemRequest> result = requestRepository.findAllByRequestor_IdOrderByCreatedDesc(user1.getId());

        assertThat(result).extracting(ItemRequest::getId)
                .containsExactly(newer.getId(), older.getId());
    }

    @Test
    @DisplayName("findAllOByIdIsNotOrderByCreatedDesc: excludes the given user's requests")
    public void findAllByIdIsNot_excludesUserRequests() {
        ItemRequest u2Request1 = newRequest(user2, "r1", LocalDateTime.now().minusDays(2));
        ItemRequest u2Request2 = newRequest(user2, "r2", LocalDateTime.now().minusDays(1));
        ItemRequest u1Request = newRequest(user1, "mine", LocalDateTime.now());
        em.flush();

        List<ItemRequest> result = requestRepository.findAllOByIdIsNotOrderByCreatedDesc(u1Request.getId());

        assertThat(result).extracting(ItemRequest::getId)
                .containsExactly(u2Request2.getId(), u2Request1.getId());
    }

    @Test
    @DisplayName("findAllByRequestor_IdOrderByCreatedDesc: returns empty when user has no requests")
    public void findAllByRequestorId_emptyWhenNone() {
        List<ItemRequest> result = requestRepository.findAllByRequestor_IdOrderByCreatedDesc(user1.getId());
        assertThat(result).isEmpty();
    }

    private User newUser(String name, String email) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        return em.persist(u);
    }

    private ItemRequest newRequest(User requestor, String description, LocalDateTime created) {
        ItemRequest r = new ItemRequest();
        r.setRequestor(requestor);
        r.setDescription(description);
        r.setCreated(created);
        return em.persist(r);
    }
}
