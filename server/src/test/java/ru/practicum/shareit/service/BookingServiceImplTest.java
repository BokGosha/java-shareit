package ru.practicum.shareit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UserIsNotOwnerException;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserIdDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserService userService;
    @Mock
    private ItemService itemService;
    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User booker;
    private User owner;
    private Item item;
    private Booking booking;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        booker = new User();
        booker.setId(2L);

        owner = new User();
        owner.setId(1L);

        item = new Item();
        item.setId(10L);
        item.setIsAvailable(true);
        item.setOwner(owner);

        booking = new Booking();
        booking.setId(100L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.WAITING);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));

        bookingDto = new BookingDto(
                100L,
                booking.getStart(),
                booking.getEnd(),
                new UserIdDto(2L),
                new ItemShortDto(10L, "Drill"),
                Status.WAITING
        );
    }

    @Test
    @DisplayName("createBooking: saves booking with WAITING status")
    void createBooking_whenValid_saves() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingCreateDto dto = new BookingCreateDto(10L, start, end);

        when(userService.existsById(2L)).thenReturn(booker);
        when(itemService.existsById(10L)).thenReturn(item);
        when(bookingRepository.existsByItem_IdAndStatusAndStartLessThanAndEndGreaterThan(
                eq(10L), eq(Status.APPROVED), any(), any())).thenReturn(false);
        when(bookingMapper.mapBookingCreateDtoToBooking(dto)).thenReturn(booking);
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.mapBookingToBookingDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.createBooking(2L, dto);

        assertThat(result).isEqualTo(bookingDto);
        assertThat(booking.getStatus()).isEqualTo(Status.WAITING);
        verify(bookingRepository).save(booking);
    }

    @Test
    @DisplayName("createBooking: throws when item unavailable")
    void createBooking_whenItemUnavailable_throws() {
        item.setIsAvailable(false);
        BookingCreateDto dto = new BookingCreateDto(
                10L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        when(userService.existsById(2L)).thenReturn(booker);
        when(itemService.existsById(10L)).thenReturn(item);

        assertThatThrownBy(() -> bookingService.createBooking(2L, dto))
                .isInstanceOf(BadRequestException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateBooking: approves when owner approves")
    void updateBooking_whenOwnerApproves_setsApproved() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingMapper.mapBookingToBookingDto(booking)).thenReturn(bookingDto);

        bookingService.updateBooking(100L, 1L, true);

        assertThat(booking.getStatus()).isEqualTo(Status.APPROVED);
    }

    @Test
    @DisplayName("updateBooking: throws when caller is not owner")
    void updateBooking_whenNotOwner_throws() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.updateBooking(100L, 999L, true))
                .isInstanceOf(UserIsNotOwnerException.class);
    }

    @Test
    @DisplayName("getBookingById: returns booking for booker")
    void getBookingById_whenBooker_returns() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(userService.existsById(2L)).thenReturn(booker);
        when(bookingMapper.mapBookingToBookingDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.getBookingById(100L, 2L);

        assertThat(result).isEqualTo(bookingDto);
    }

    @Test
    @DisplayName("getBookingById: throws when caller is neither booker nor owner")
    void getBookingById_whenStranger_throws() {
        User stranger = new User();
        stranger.setId(999L);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(userService.existsById(999L)).thenReturn(stranger);

        assertThatThrownBy(() -> bookingService.getBookingById(100L, 999L))
                .isInstanceOf(UserIsNotOwnerException.class);
    }

    @Test
    @DisplayName("existsById: returns booking when found")
    void existsById_whenFound_returns() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThat(bookingService.existsById(100L)).isSameAs(booking);
    }

    @Test
    @DisplayName("existsById: throws when missing")
    void existsById_whenMissing_throws() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.existsById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("getBookingsByBookerIdAndState: ALL state returns bookings")
    void getBookingsByBookerIdAndState_all_returns() {
        when(userService.existsById(2L)).thenReturn(booker);
        when(bookingRepository.findAllByBooker_IdOrderByStartDesc(2L)).thenReturn(List.of(booking));
        when(bookingMapper.mapBookingToBookingDto(booking)).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getBookingsByBookerIdAndState(2L, "ALL");

        assertThat(result).containsExactly(bookingDto);
    }

    @Test
    @DisplayName("getBookingsByBookerIdAndState: invalid state throws")
    void getBookingsByBookerIdAndState_invalidState_throws() {
        when(userService.existsById(anyLong())).thenReturn(booker);

        assertThatThrownBy(() -> bookingService.getBookingsByBookerIdAndState(2L, "UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("getBookingsByOwnerIdAndState: WAITING state filters by status")
    void getBookingsByOwnerIdAndState_waiting_returns() {
        when(userService.existsById(1L)).thenReturn(owner);
        when(bookingRepository.findAllByBooker_IdAndStatusOrderByStartDesc(1L, Status.WAITING))
                .thenReturn(List.of(booking));
        when(bookingMapper.mapBookingToBookingDto(booking)).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getBookingsByOwnerIdAndState(1L, "WAITING");

        assertThat(result).containsExactly(bookingDto);
    }

    @Test
    @DisplayName("getBookingsByOwnerIdAndState: REJECTED state empty")
    void getBookingsByOwnerIdAndState_rejected_empty() {
        when(userService.existsById(1L)).thenReturn(owner);
        when(bookingRepository.findAllByBooker_IdAndStatusOrderByStartDesc(1L, Status.REJECTED))
                .thenReturn(List.of());

        List<BookingDto> result = bookingService.getBookingsByOwnerIdAndState(1L, "REJECTED");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("createBooking: throws when end is before now")
    void createBooking_whenEndInPast_throws() {
        BookingCreateDto dto = new BookingCreateDto(
                10L,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1));

        when(userService.existsById(2L)).thenReturn(booker);
        when(itemService.existsById(10L)).thenReturn(item);

        assertThatThrownBy(() -> bookingService.createBooking(2L, dto))
                .isInstanceOf(BadRequestException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBooking: throws when end equals start")
    void createBooking_whenEndEqualsStart_throws() {
        LocalDateTime sameMoment = LocalDateTime.now().plusDays(1);
        BookingCreateDto dto = new BookingCreateDto(10L, sameMoment, sameMoment);

        when(userService.existsById(2L)).thenReturn(booker);
        when(itemService.existsById(10L)).thenReturn(item);

        assertThatThrownBy(() -> bookingService.createBooking(2L, dto))
                .isInstanceOf(BadRequestException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBooking: throws when overlapping booking exists")
    void createBooking_whenOverlap_throws() {
        BookingCreateDto dto = new BookingCreateDto(
                10L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        when(userService.existsById(2L)).thenReturn(booker);
        when(itemService.existsById(10L)).thenReturn(item);
        when(bookingRepository.existsByItem_IdAndStatusAndStartLessThanAndEndGreaterThan(
                eq(10L), eq(Status.APPROVED), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> bookingService.createBooking(2L, dto))
                .isInstanceOf(BadRequestException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateBooking: rejects when approved=false")
    void updateBooking_whenOwnerRejects_setsRejected() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingMapper.mapBookingToBookingDto(booking)).thenReturn(bookingDto);

        bookingService.updateBooking(100L, 1L, false);

        assertThat(booking.getStatus()).isEqualTo(Status.REJECTED);
    }

    @Test
    @DisplayName("getBookingById: returns booking for owner")
    void getBookingById_whenOwner_returns() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(userService.existsById(1L)).thenReturn(owner);
        when(bookingMapper.mapBookingToBookingDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.getBookingById(100L, 1L);

        assertThat(result).isEqualTo(bookingDto);
    }

    @Test
    @DisplayName("getBookingsByBookerIdAndState: CURRENT delegates to current finder")
    void getBookingsByBookerIdAndState_current_returns() {
        when(userService.existsById(2L)).thenReturn(booker);
        when(bookingRepository.findAllByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(
                eq(2L), any(), any())).thenReturn(List.of(booking));
        when(bookingMapper.mapBookingToBookingDto(booking)).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getBookingsByBookerIdAndState(2L, "CURRENT");

        assertThat(result).containsExactly(bookingDto);
    }

    @Test
    @DisplayName("getBookingsByBookerIdAndState: PAST delegates to past finder")
    void getBookingsByBookerIdAndState_past_returns() {
        when(userService.existsById(2L)).thenReturn(booker);
        when(bookingRepository.findAllByBooker_IdAndEndBeforeOrderByStartDesc(eq(2L), any()))
                .thenReturn(List.of(booking));
        when(bookingMapper.mapBookingToBookingDto(booking)).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getBookingsByBookerIdAndState(2L, "PAST");

        assertThat(result).containsExactly(bookingDto);
    }

    @Test
    @DisplayName("getBookingsByBookerIdAndState: FUTURE delegates to future finder")
    void getBookingsByBookerIdAndState_future_returns() {
        when(userService.existsById(2L)).thenReturn(booker);
        when(bookingRepository.findAllByBooker_IdAndStartAfterOrderByStartDesc(eq(2L), any()))
                .thenReturn(List.of(booking));
        when(bookingMapper.mapBookingToBookingDto(booking)).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getBookingsByBookerIdAndState(2L, "FUTURE");

        assertThat(result).containsExactly(bookingDto);
    }
}
