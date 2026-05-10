package ru.practicum.shareit.integration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestWithResponsesDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceIntegrationTest {

    private final ItemRequestService itemRequestService;

    @PersistenceContext
    private EntityManager em;

    @Test
    void getUserRequests_returnsRequestsOrderedByCreatedDescWithResponses() {
        User requestor = persistUser("Req", "req@example.com");
        User otherUser = persistUser("Other", "other@example.com");
        User responder = persistUser("Responder", "resp@example.com");

        LocalDateTime now = LocalDateTime.now();
        ItemRequest oldReq = persistRequest(requestor, "need a drill", now.minusDays(2));
        ItemRequest newReq = persistRequest(requestor, "need a saw", now.minusDays(1));
        ItemRequest foreignReq = persistRequest(otherUser, "foreign", now);

        persistItem(responder, "Drill A", "drill", true, oldReq);
        persistItem(responder, "Drill B", "drill 2", true, oldReq);
        persistItem(responder, "Saw", "saw", true, newReq);
        persistItem(responder, "Foreign Item", "x", true, foreignReq);

        em.flush();
        em.clear();

        List<ItemRequestWithResponsesDto> result =
                itemRequestService.getUserRequests(requestor.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).description()).isEqualTo("need a saw");
        assertThat(result.get(0).items())
                .extracting("name")
                .containsExactly("Saw");

        assertThat(result.get(1).description()).isEqualTo("need a drill");
        assertThat(result.get(1).items())
                .extracting("name")
                .containsExactlyInAnyOrder("Drill A", "Drill B");
    }

    private User persistUser(String name, String email) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        em.persist(u);
        return u;
    }

    private ItemRequest persistRequest(User requestor, String description, LocalDateTime created) {
        ItemRequest r = new ItemRequest();
        r.setRequestor(requestor);
        r.setDescription(description);
        r.setCreated(created);
        em.persist(r);
        return r;
    }

    private void persistItem(User owner, String name, String desc, boolean available, ItemRequest request) {
        Item i = new Item();
        i.setOwner(owner);
        i.setName(name);
        i.setDescription(desc);
        i.setIsAvailable(available);
        i.setRequest(request);
        em.persist(i);
    }
}
