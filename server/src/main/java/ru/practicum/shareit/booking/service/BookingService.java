package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingService {

    BookingDto createBooking(Long bookerId, BookingCreateDto bookingCreateDto);

    BookingDto updateBooking(Long bookingId, Long ownerId, Boolean approved);

    BookingDto getBookingById(Long bookingId, Long bookerOrOwnerId);

    Booking existsById(Long bookingId);

    List<BookingDto> getBookingsByBookerIdAndState(Long bookerId, String state);

    List<BookingDto> getBookingsByOwnerIdAndState(Long ownerId, String state);
}
