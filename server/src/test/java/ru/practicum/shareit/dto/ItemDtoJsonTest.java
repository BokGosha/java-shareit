package ru.practicum.shareit.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMoreDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemDtoJsonTest {

    private final JacksonTester<ItemDto> itemJson;

    private final JacksonTester<ItemMoreDto> itemMoreJson;

    private final JacksonTester<ItemCreateDto> createJson;

    private final JacksonTester<ItemUpdateDto> updateJson;

    private final JacksonTester<CommentDto> commentJson;

    private final JacksonTester<CommentCreateDto> commentCreateJson;

    @Test
    public void serializeItemDto_withDefaultEmptyComments() throws Exception {
        ItemDto dto = new ItemDto(1L, "Drill", "Powerful drill", true, null);

        JsonContent<ItemDto> json = itemJson.write(dto);

        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json).extractingJsonPathStringValue("$.name").isEqualTo("Drill");
        assertThat(json).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(json).extractingJsonPathArrayValue("$.comments").isEmpty();
    }

    @Test
    public void serializeItemMoreDto_withBookingsAndComments() throws Exception {
        LocalDateTime created = LocalDateTime.of(2026, 5, 1, 10, 0);
        BookingShortDto last = new BookingShortDto(1L, 1L, 7L,
                LocalDateTime.of(2026, 4, 1, 10, 0),
                LocalDateTime.of(2026, 4, 2, 10, 0));
        BookingShortDto next = new BookingShortDto(2L, 1L, 8L,
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 6, 2, 10, 0));
        CommentDto comment = new CommentDto(10L, "Nice", "Bob", created);
        ItemMoreDto dto = new ItemMoreDto(1L, "Drill", "desc", true, last, next, List.of(comment));

        JsonContent<ItemMoreDto> json = itemMoreJson.write(dto);

        assertThat(json).extractingJsonPathNumberValue("$.lastBooking.id").isEqualTo(1);
        assertThat(json).extractingJsonPathNumberValue("$.lastBooking.bookerId").isEqualTo(7);
        assertThat(json).extractingJsonPathNumberValue("$.nextBooking.id").isEqualTo(2);
        assertThat(json).extractingJsonPathStringValue("$.comments[0].authorName").isEqualTo("Bob");
        assertThat(json).extractingJsonPathStringValue("$.comments[0].created").isEqualTo("2026-05-01T10:00:00");
    }

    @Test
    public void deserializeItemCreateDto() throws Exception {
        String content = """
            {"name":"Drill","description":"desc","available":true,"requestId":5}
            """;

        ItemCreateDto dto = createJson.parseObject(content);

        assertThat(dto.name()).isEqualTo("Drill");
        assertThat(dto.description()).isEqualTo("desc");
        assertThat(dto.available()).isTrue();
        assertThat(dto.requestId()).isEqualTo(5L);
    }

    @Test
    public void deserializeItemUpdateDto_partial() throws Exception {
        String content = "{\"name\":\"Updated\"}";

        ItemUpdateDto dto = updateJson.parseObject(content);

        assertThat(dto.name()).isEqualTo("Updated");
        assertThat(dto.description()).isNull();
        assertThat(dto.available()).isNull();
    }

    @Test
    public void serializeCommentDto() throws Exception {
        CommentDto dto = new CommentDto(1L, "text", "Alice", LocalDateTime.of(2026, 1, 1, 0, 0));

        JsonContent<CommentDto> json = commentJson.write(dto);

        assertThat(json).extractingJsonPathStringValue("$.text").isEqualTo("text");
        assertThat(json).extractingJsonPathStringValue("$.authorName").isEqualTo("Alice");
        assertThat(json).extractingJsonPathStringValue("$.created").isEqualTo("2026-01-01T00:00:00");
    }

    @Test
    public void deserializeCommentCreateDto() throws Exception {
        CommentCreateDto dto = commentCreateJson.parseObject("{\"text\":\"hi\"}");

        assertThat(dto.text()).isEqualTo("hi");
    }
}
