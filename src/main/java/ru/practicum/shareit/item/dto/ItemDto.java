package ru.practicum.shareit.item.dto;

import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.user.dto.UserDto;

@Data
public class ItemDto {

    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private UserDto owner;
    private BookingShortDto lastBooking;
    private BookingShortDto nextBooking;
}
