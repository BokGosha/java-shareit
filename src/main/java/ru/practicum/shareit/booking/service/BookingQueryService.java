package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;
import java.util.Map;

public interface BookingQueryService {

    Map<Long, List<BookingShortDto>> getApprovedBookingsByItemIds(List<Long> itemIds);

    List<Booking> getApprovedBookingsByItemIdAndBookerId(Long itemId, Long bookerId);

    List<BookingShortDto> getApprovedBookingsByItemId(Long itemId);
}
