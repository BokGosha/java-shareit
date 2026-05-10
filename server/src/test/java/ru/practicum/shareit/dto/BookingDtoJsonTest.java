package ru.practicum.shareit.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.dto.UserIdDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingDtoJsonTest {

    private final JacksonTester<BookingDto> bookingJson;

    private final JacksonTester<BookingCreateDto> createJson;

    @Test
    public void serializeBookingDto() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 5, 10, 12, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 11, 12, 0, 0);
        BookingDto dto = new BookingDto(
                42L, start, end,
                new UserIdDto(7L),
                new ItemShortDto(3L, "Drill"),
                Status.APPROVED
        );

        JsonContent<BookingDto> json = bookingJson.write(dto);

        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(42);
        assertThat(json).extractingJsonPathStringValue("$.start").isEqualTo("2026-05-10T12:00:00");
        assertThat(json).extractingJsonPathStringValue("$.end").isEqualTo("2026-05-11T12:00:00");
        assertThat(json).extractingJsonPathNumberValue("$.booker.id").isEqualTo(7);
        assertThat(json).extractingJsonPathNumberValue("$.item.id").isEqualTo(3);
        assertThat(json).extractingJsonPathStringValue("$.item.name").isEqualTo("Drill");
        assertThat(json).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");
    }

    @Test
    public void deserializeBookingCreateDto() throws Exception {
        String content = "{\"itemId\":5,\"start\":\"2026-06-01T10:00:00\",\"end\":\"2026-06-02T10:00:00\"}";

        BookingCreateDto dto = createJson.parseObject(content);

        assertThat(dto.itemId()).isEqualTo(5L);
        assertThat(dto.start()).isEqualTo(LocalDateTime.of(2026, 6, 1, 10, 0));
        assertThat(dto.end()).isEqualTo(LocalDateTime.of(2026, 6, 2, 10, 0));
    }
}
