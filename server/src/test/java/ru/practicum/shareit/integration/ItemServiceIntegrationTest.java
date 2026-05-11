package ru.practicum.shareit.integration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.assertj.core.data.TemporalOffset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemMoreDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceIntegrationTest {

    private final ItemService itemService;

    @PersistenceContext
    private EntityManager em;

    @Test
    void getItemsByOwnerId_returnsItemsWithLastNextBookingAndComments() {
        User owner = persistUser("Owner", "owner@example.com");
        User booker = persistUser("Booker", "booker@example.com");

        Item drill = persistItem(owner, "Drill", "Cordless drill", true);
        Item saw = persistItem(owner, "Saw", "Hand saw", true);

        LocalDateTime now = LocalDateTime.now();
        persistBooking(drill, booker, now.minusDays(5), now.minusDays(4), Status.APPROVED);
        persistBooking(drill, booker, now.minusDays(2), now.minusDays(1), Status.APPROVED);
        persistBooking(drill, booker, now.plusDays(1), now.plusDays(2), Status.APPROVED);
        persistBooking(drill, booker, now.plusDays(5), now.plusDays(6), Status.APPROVED);
        persistBooking(saw, booker, now.minusDays(10), now.minusDays(9), Status.WAITING);

        persistComment(drill, booker, "Great drill", now.minusDays(3));
        persistComment(drill, booker, "Worked well", now.minusDays(1));

        em.flush();
        em.clear();

        List<ItemMoreDto> items = itemService.getItemsByOwnerId(owner.getId());

        assertThat(items).hasSize(2);

        ItemMoreDto drillDto = items.stream()
                .filter(i -> i.name().equals("Drill"))
                .findFirst().orElseThrow();
        assertThat(drillDto.lastBooking()).isNotNull();
        assertThat(drillDto.lastBooking().start()).isCloseTo(now.minusDays(2),
                within());
        assertThat(drillDto.nextBooking()).isNotNull();
        assertThat(drillDto.nextBooking().start()).isCloseTo(now.plusDays(1),
                within());
        assertThat(drillDto.comments())
                .extracting("text")
                .containsExactlyInAnyOrder("Great drill", "Worked well");

        ItemMoreDto sawDto = items.stream()
                .filter(i -> i.name().equals("Saw"))
                .findFirst().orElseThrow();
        assertThat(sawDto.lastBooking()).isNull();
        assertThat(sawDto.nextBooking()).isNull();
        assertThat(sawDto.comments()).isEmpty();
    }

    private static TemporalOffset<? super LocalDateTime> within() {
        return new org.assertj.core.data.TemporalUnitWithinOffset(1, java.time.temporal.ChronoUnit.MINUTES);
    }

    private User persistUser(String name, String email) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        em.persist(u);
        return u;
    }

    private Item persistItem(User owner, String name, String desc, boolean available) {
        Item i = new Item();
        i.setOwner(owner);
        i.setName(name);
        i.setDescription(desc);
        i.setIsAvailable(available);
        em.persist(i);
        return i;
    }

    private void persistBooking(Item item, User booker, LocalDateTime start, LocalDateTime end, Status status) {
        Booking b = new Booking();
        b.setItem(item);
        b.setBooker(booker);
        b.setStart(start);
        b.setEnd(end);
        b.setStatus(status);
        em.persist(b);
    }

    private void persistComment(Item item, User author, String text, LocalDateTime created) {
        Comment c = new Comment();
        c.setItem(item);
        c.setAuthor(author);
        c.setText(text);
        c.setCreated(created);
        em.persist(c);
    }
}
