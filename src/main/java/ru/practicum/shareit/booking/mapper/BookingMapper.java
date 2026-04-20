package ru.practicum.shareit.booking.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
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
        bookingDto.setItemName(booking.getItem().getName());

        User booker = booking.getBooker();
        UserDto bookerDto = UserMapper.mapUserToUserDto(booker);
        bookingDto.setBooker(bookerDto);

        Item item = booking.getItem();
        ItemDto itemDto = ItemMapper.mapItemToItemDto(item);
        bookingDto.setItem(itemDto);

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
