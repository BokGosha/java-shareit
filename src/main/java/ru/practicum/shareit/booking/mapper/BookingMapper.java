package ru.practicum.shareit.booking.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserIdDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BookingMapper {

    public static BookingDto mapBookingToBookingDto(Booking booking) {
        BookingDto bookingDto = new BookingDto();

        bookingDto.setId(booking.getId());
        bookingDto.setStart(booking.getStart());
        bookingDto.setEnd(booking.getEnd());
        bookingDto.setStatus(booking.getStatus());

        User booker = booking.getBooker();
        UserIdDto bookerIdDto = UserMapper.mapUserToUserIdDto(booker);
        bookingDto.setBooker(bookerIdDto);

        Item item = booking.getItem();
        ItemShortDto itemShortDto = ItemMapper.mapItemToItemShortDto(item);
        bookingDto.setItem(itemShortDto);

        return bookingDto;
    }

    public static Booking mapBookingCreateDtoToBooking(BookingCreateDto bookingCreateDto) {
        Booking booking = new Booking();

        booking.setStart(bookingCreateDto.getStart());
        booking.setEnd(bookingCreateDto.getEnd());

        return booking;
    }

    public static BookingShortDto mapBookingToBookingShortDto(Booking booking) {
        BookingShortDto bookingShortDto = new BookingShortDto();

        bookingShortDto.setId(booking.getId());
        bookingShortDto.setStart(booking.getStart());
        bookingShortDto.setEnd(booking.getEnd());
        bookingShortDto.setItemId(booking.getItem().getId());
        bookingShortDto.setBookerId(booking.getBooker().getId());

        return bookingShortDto;
    }
}
