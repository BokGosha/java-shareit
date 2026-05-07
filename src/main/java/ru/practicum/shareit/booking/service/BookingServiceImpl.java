package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UserIsNotOwnerException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    private final UserService userService;
    private final ItemService itemService;

    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingDto createBooking(Long bookerId, BookingCreateDto bookingCreateDto) {
        User booker = userService.existsById(bookerId);
        Item bookingItem = itemService.existsById(bookingCreateDto.itemId());

        validateBooking(bookingItem, bookingCreateDto);

        Booking booking = bookingMapper.mapBookingCreateDtoToBooking(bookingCreateDto);
        booking.setItem(bookingItem);
        booking.setBooker(booker);
        booking.setStatus(Status.WAITING);

        booking = bookingRepository.save(booking);

        return bookingMapper.mapBookingToBookingDto(booking);
    }

    @Override
    @Transactional
    public BookingDto updateBooking(Long bookingId, Long ownerId, Boolean approved) {
        Booking booking = existsById(bookingId);

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new UserIsNotOwnerException("Пользователь с id=" + ownerId
                    + " не является владельцем вещи с id=" + booking.getItem().getId());
        }

        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);

        return bookingMapper.mapBookingToBookingDto(booking);
    }

    @Override
    public BookingDto getBookingById(Long bookingId, Long bookerOrOwnerId) {
        Booking booking = existsById(bookingId);
        userService.existsById(bookerOrOwnerId);

        if (!(booking.getBooker().getId().equals(bookerOrOwnerId)
                || booking.getItem().getOwner().getId().equals(bookerOrOwnerId))) {
            throw new UserIsNotOwnerException("Пользователь с id=" + bookerOrOwnerId
                    + " не является владельцем или автором бронирования вещи с id=" + booking.getItem().getId());
        }

        return bookingMapper.mapBookingToBookingDto(booking);
    }

    @Override
    public Booking existsById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь с id=" + bookingId + " не найдена"));
    }

    @Override
    public List<BookingDto> getBookingsByBookerIdAndState(Long bookerId, String state) {
        userService.existsById(bookerId);

        return getBookingsByUserId(bookerId, state);
    }

    @Override
    public List<BookingDto> getBookingsByOwnerIdAndState(Long ownerId, String state) {
        userService.existsById(ownerId);

        return getBookingsByUserId(ownerId, state);
    }

    private List<BookingDto> getBookingsByUserId(Long userId, String state) {
        BookingState bookingState = BookingState.valueOf(state);
        userService.existsById(userId);

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (bookingState) {
            case ALL -> bookingRepository.findAllByBooker_IdOrderByStartDesc(userId);
            case CURRENT ->
                    bookingRepository.findAllByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now);
            case PAST -> bookingRepository.findAllByBooker_IdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE -> bookingRepository.findAllByBooker_IdAndStartAfterOrderByStartDesc(userId, now);
            case WAITING -> bookingRepository.findAllByBooker_IdAndStatusOrderByStartDesc(userId, Status.WAITING);
            case REJECTED -> bookingRepository.findAllByBooker_IdAndStatusOrderByStartDesc(userId, Status.REJECTED);
        };

        return bookings.stream()
                .map(bookingMapper::mapBookingToBookingDto)
                .toList();
    }

    private void validateBooking(Item bookingItem, BookingCreateDto bookingCreateDto) {
        validateStartAndEnd(bookingCreateDto.start(), bookingCreateDto.end());

        if (!bookingItem.getIsAvailable()) {
            throw new BadRequestException("Вещь с id=" + bookingItem.getId()
                    + " недоступна для бронирования");
        }

        boolean overlap = bookingRepository
                .existsByItem_IdAndStatusAndStartLessThanAndEndGreaterThan(
                        bookingItem.getId(),
                        Status.APPROVED,
                        bookingCreateDto.end(),
                        bookingCreateDto.start());
        if (overlap) {
            throw new BadRequestException("Вещь уже забронирована на эти даты");
        }
    }

    private void validateStartAndEnd(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();

        if (end.isBefore(now)) {
            throw new BadRequestException("Время завершения аренды в прошлом");
        }

        if (end.isEqual(start)) {
            throw new BadRequestException("Время завершения аренды совпадает с временем старта");
        }
    }
}
