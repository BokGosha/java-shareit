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
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingRepositoryTest {

    private final TestEntityManager em;

    private final BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private Item item;
    private LocalDateTime now;

    @BeforeEach
    public void setUp() {
        owner = newUser("Owner", "owner@example.com");
        booker = newUser("Booker", "booker@example.com");
        item = newItem("Drill", owner);
        now = LocalDateTime.now();
    }

    @Test
    @DisplayName("findAllByBooker_IdOrderByStartDesc: returns all booker bookings sorted by start desc")
    public void findAllByBookerId_orderByStartDesc() {
        Booking past = newBooking(item, booker, now.minusDays(5), now.minusDays(4), Status.APPROVED);
        Booking future = newBooking(item, booker, now.plusDays(1), now.plusDays(2), Status.WAITING);
        Booking otherUser = newBooking(item, owner, now.plusDays(3), now.plusDays(4), Status.WAITING);
        em.flush();

        List<Booking> result = bookingRepository.findAllByBooker_IdOrderByStartDesc(booker.getId());

        assertThat(result).extracting(Booking::getId)
                .containsExactly(future.getId(), past.getId());
        assertThat(result).extracting(Booking::getId).doesNotContain(otherUser.getId());
    }

    @Test
    @DisplayName("findAllByBooker_IdAndEndBeforeOrderByStartDesc: returns past bookings only")
    public void findAllByBookerId_pastBookings() {
        Booking past = newBooking(item, booker, now.minusDays(5), now.minusDays(4), Status.APPROVED);
        newBooking(item, booker, now.plusDays(1), now.plusDays(2), Status.WAITING);
        em.flush();

        List<Booking> result = bookingRepository.findAllByBooker_IdAndEndBeforeOrderByStartDesc(booker.getId(), now);

        assertThat(result).extracting(Booking::getId).containsExactly(past.getId());
    }

    @Test
    @DisplayName("findAllByBooker_IdAndStartAfterOrderByStartDesc: returns future bookings only")
    public void findAllByBookerId_futureBookings() {
        newBooking(item, booker, now.minusDays(5), now.minusDays(4), Status.APPROVED);
        Booking future = newBooking(item, booker, now.plusDays(1), now.plusDays(2), Status.WAITING);
        em.flush();

        List<Booking> result = bookingRepository.findAllByBooker_IdAndStartAfterOrderByStartDesc(booker.getId(), now);

        assertThat(result).extracting(Booking::getId).containsExactly(future.getId());
    }

    @Test
    @DisplayName("findAllByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc: returns current bookings")
    public void findAllByBookerId_currentBookings() {
        Booking current = newBooking(item, booker, now.minusHours(1), now.plusHours(1), Status.APPROVED);
        newBooking(item, booker, now.plusDays(1), now.plusDays(2), Status.WAITING);
        em.flush();

        List<Booking> result = bookingRepository
                .findAllByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(booker.getId(), now, now);

        assertThat(result).extracting(Booking::getId).containsExactly(current.getId());
    }

    @Test
    @DisplayName("findAllByBooker_IdAndStatusOrderByStartDesc: filters by status")
    public void findAllByBookerId_byStatus() {
        Booking waiting = newBooking(item, booker, now.plusDays(1), now.plusDays(2), Status.WAITING);
        newBooking(item, booker, now.plusDays(3), now.plusDays(4), Status.APPROVED);
        em.flush();

        List<Booking> result = bookingRepository.findAllByBooker_IdAndStatusOrderByStartDesc(booker.getId(), Status.WAITING);

        assertThat(result).extracting(Booking::getId).containsExactly(waiting.getId());
    }

    @Test
    @DisplayName("findAllByItem_IdInAndStatus: filters bookings by item ids and status")
    public void findAllByItemIdIn_andStatus() {
        Item second = newItem("Saw", owner);
        Booking approvedDrill = newBooking(item, booker, now.plusDays(1), now.plusDays(2), Status.APPROVED);
        newBooking(item, booker, now.plusDays(3), now.plusDays(4), Status.WAITING);
        Booking approvedSaw = newBooking(second, booker, now.plusDays(5), now.plusDays(6), Status.APPROVED);
        em.flush();

        List<Booking> result = bookingRepository.findAllByItem_IdInAndStatus(
                List.of(item.getId(), second.getId()), Status.APPROVED);

        assertThat(result).extracting(Booking::getId)
                .containsExactlyInAnyOrder(approvedDrill.getId(), approvedSaw.getId());
    }

    @Test
    @DisplayName("existsByItem_IdAndStatusAndStartLessThanAndEndGreaterThan: detects overlap")
    public void existsByItemId_andDateOverlap() {
        newBooking(item, booker, now.plusDays(1), now.plusDays(3), Status.APPROVED);
        em.flush();

        boolean overlap = bookingRepository.existsByItem_IdAndStatusAndStartLessThanAndEndGreaterThan(
                item.getId(), Status.APPROVED, now.plusDays(4), now.plusDays(2));
        boolean noOverlap = bookingRepository.existsByItem_IdAndStatusAndStartLessThanAndEndGreaterThan(
                item.getId(), Status.APPROVED, now.plusDays(10), now.plusDays(5));

        assertThat(overlap).isTrue();
        assertThat(noOverlap).isFalse();
    }

    @Test
    @DisplayName("findAllByItem_IdAndBooker_IdAndStatus: finds completed booking by booker for item")
    public void findAllByItemIdAndBookerIdAndStatus() {
        Booking approved = newBooking(item, booker, now.minusDays(2), now.minusDays(1), Status.APPROVED);
        newBooking(item, booker, now.plusDays(1), now.plusDays(2), Status.WAITING);
        em.flush();

        List<Booking> result = bookingRepository.findAllByItem_IdAndBooker_IdAndStatus(
                item.getId(), booker.getId(), Status.APPROVED);

        assertThat(result).extracting(Booking::getId).containsExactly(approved.getId());
    }

    private User newUser(String name, String email) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        return em.persist(u);
    }

    private Item newItem(String name, User owner) {
        Item item = new Item();
        item.setName(name);
        item.setDescription("desc");
        item.setIsAvailable(true);
        item.setOwner(owner);
        return em.persist(item);
    }

    private Booking newBooking(Item item, User booker, LocalDateTime start, LocalDateTime end, Status status) {
        Booking b = new Booking();
        b.setItem(item);
        b.setBooker(booker);
        b.setStart(start);
        b.setEnd(end);
        b.setStatus(status);
        return em.persist(b);
    }
}
