package ru.practicum.shareit.integration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceIntegrationTest {

    private final BookingService bookingService;

    @PersistenceContext
    private EntityManager em;

    @Test
    void createBooking_persistsWaitingBookingForAvailableItem() {
        User owner = persistUser("Owner", "owner-b@example.com");
        User booker = persistUser("Booker", "booker-b@example.com");
        Item item = persistItem(owner, "Drill", "Cordless drill", true);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(1);

        BookingDto created = bookingService.createBooking(
                booker.getId(), new BookingCreateDto(item.getId(), start, end));

        assertThat(created.id()).isNotNull();
        assertThat(created.status()).isEqualTo(Status.WAITING);
        assertThat(created.booker().id()).isEqualTo(booker.getId());
        assertThat(created.item().id()).isEqualTo(item.getId());

        Booking stored = em.find(Booking.class, created.id());
        assertThat(stored).isNotNull();
        assertThat(stored.getStatus()).isEqualTo(Status.WAITING);
        assertThat(stored.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(stored.getItem().getId()).isEqualTo(item.getId());
    }

    @Test
    void createBooking_whenItemUnavailable_throwsBadRequest() {
        User owner = persistUser("Owner", "owner-u@example.com");
        User booker = persistUser("Booker", "booker-u@example.com");
        Item item = persistItem(owner, "Drill", "Broken drill", false);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        BookingCreateDto dto = new BookingCreateDto(item.getId(), start, start.plusDays(1));

        assertThatThrownBy(() -> bookingService.createBooking(booker.getId(), dto))
                .isInstanceOf(BadRequestException.class);

        Long count = em.createQuery(
                        "select count(b) from Booking b where b.item.id = :id", Long.class)
                .setParameter("id", item.getId())
                .getSingleResult();
        assertThat(count).isZero();
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
}
