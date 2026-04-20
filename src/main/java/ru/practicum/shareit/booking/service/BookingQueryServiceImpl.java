package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingQueryServiceImpl implements BookingQueryService {

    private final BookingRepository bookingRepository;

    @Override
    public Map<Long, List<BookingShortDto>> getApprovedBookingsByItemIds(List<Long> itemIds) {
        if (itemIds.isEmpty()) {
            return Map.of();
        }
        return bookingRepository
                .findAllByItem_IdInAndStatus(itemIds, Status.APPROVED)
                .stream()
                .map(BookingMapper::mapBookingToBookingShortDto)
                .collect(Collectors.groupingBy(BookingShortDto::getItemId));
    }
}
