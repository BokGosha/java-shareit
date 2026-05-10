package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UserIsNotOwnerException;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.dto.UserIdDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingDto bookingDto;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    public void setUp() {
        start = LocalDateTime.now().plusDays(1).withNano(0);
        end = LocalDateTime.now().plusDays(2).withNano(0);
        bookingDto = new BookingDto(
                1L,
                start,
                end,
                new UserIdDto(1L),
                new ItemShortDto(1L, "Drill"),
                Status.WAITING
        );
    }

    @Test
    @DisplayName("GET /bookings")
    public void getBookingsByBookerIdAndState_shouldReturnList() throws Exception {
        when(bookingService.getBookingsByBookerIdAndState(1L, "ALL")).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings").header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("WAITING"));

        verify(bookingService).getBookingsByBookerIdAndState(1L, "ALL");
    }

    @Test
    @DisplayName("GET /bookings, with state param")
    public void getBookingsByBookerIdAndState_withState_shouldReturnList() throws Exception {
        when(bookingService.getBookingsByBookerIdAndState(1L, "FUTURE")).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, 1L)
                        .param("state", "FUTURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(bookingService).getBookingsByBookerIdAndState(1L, "FUTURE");
    }

    @Test
    @DisplayName("GET /bookings, missing X-Sharer-User-Id header")
    public void getBookings_whenHeaderMissing_shouldReturn400() throws Exception {
        mockMvc.perform(get("/bookings"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /bookings/owner")
    public void getBookingsByOwnerIdAndState_shouldReturnList() throws Exception {
        when(bookingService.getBookingsByOwnerIdAndState(1L, "ALL")).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner").header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(bookingService).getBookingsByOwnerIdAndState(1L, "ALL");
    }

    @Test
    @DisplayName("POST /bookings")
    public void createBooking_shouldReturn201() throws Exception {
        BookingCreateDto request = new BookingCreateDto(1L, start, end);
        when(bookingService.createBooking(eq(1L), any(BookingCreateDto.class))).thenReturn(bookingDto);

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(1L))
                .andExpect(jsonPath("$.item.id").value(1L))
                .andExpect(jsonPath("$.item.name").value("Drill"));

        verify(bookingService).createBooking(eq(1L), any(BookingCreateDto.class));
    }

    @Test
    @DisplayName("POST /bookings, item not found")
    public void createBooking_whenItemNotFound_shouldReturn404() throws Exception {
        BookingCreateDto request = new BookingCreateDto(999L, start, end);
        when(bookingService.createBooking(eq(1L), any(BookingCreateDto.class)))
                .thenThrow(new NotFoundException("Вещь с id=999 не найдена"));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /bookings/{bookingId} approve")
    public void updateBooking_approve_shouldReturnApproved() throws Exception {
        BookingDto approved = new BookingDto(
                1L, start, end,
                new UserIdDto(1L),
                new ItemShortDto(1L, "Drill"),
                Status.APPROVED
        );
        when(bookingService.updateBooking(1L, 1L, true)).thenReturn(approved);

        mockMvc.perform(patch("/bookings/{bookingId}", 1L)
                        .header(USER_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingService).updateBooking(1L, 1L, true);
    }

    @Test
    @DisplayName("PATCH /bookings/{bookingId}, user is not owner")
    public void updateBooking_whenUserIsNotOwner_shouldReturn403() throws Exception {
        when(bookingService.updateBooking(1L, 2L, true))
                .thenThrow(new UserIsNotOwnerException("Пользователь не является владельцем"));

        mockMvc.perform(patch("/bookings/{bookingId}", 1L)
                        .header(USER_HEADER, 2L)
                        .param("approved", "true"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /bookings/{bookingId}")
    public void getBookingById_shouldReturnBooking() throws Exception {
        when(bookingService.getBookingById(1L, 1L)).thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/{bookingId}", 1L)
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(bookingService).getBookingById(1L, 1L);
    }

    @Test
    @DisplayName("GET /bookings/{bookingId}, not found")
    public void getBookingById_whenNotFound_shouldReturn404() throws Exception {
        when(bookingService.getBookingById(999L, 1L))
                .thenThrow(new NotFoundException("Бронирование с id=999 не найдено"));

        mockMvc.perform(get("/bookings/{bookingId}", 999L)
                        .header(USER_HEADER, 1L))
                .andExpect(status().isNotFound());
    }
}
