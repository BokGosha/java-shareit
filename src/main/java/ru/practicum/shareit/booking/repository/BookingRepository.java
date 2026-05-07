package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByItem_IdInAndStatus(List<Long> itemIds, Status status);

    List<Booking> findAllByBooker_IdOrderByStartDesc(Long userId);

    List<Booking> findAllByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(Long userId,
                                                                              LocalDateTime now,
                                                                              LocalDateTime now1);

    List<Booking> findAllByBooker_IdAndEndBeforeOrderByStartDesc(Long userId, LocalDateTime now);

    List<Booking> findAllByBooker_IdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime now);

    List<Booking> findAllByBooker_IdAndStatusOrderByStartDesc(Long userId, Status status);

    boolean existsByItem_IdAndStatusAndStartLessThanAndEndGreaterThan(Long id,
                                                                      Status status,
                                                                      LocalDateTime end,
                                                                      LocalDateTime start);

    List<Booking> findAllByItem_IdAndBooker_IdAndStatus(Long itemId, Long bookerId, Status status);
}
