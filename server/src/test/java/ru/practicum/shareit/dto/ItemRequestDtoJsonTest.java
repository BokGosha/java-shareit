package ru.practicum.shareit.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithResponsesDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestDtoJsonTest {

    private final JacksonTester<ItemRequestDto> requestJson;

    private final JacksonTester<ItemRequestWithResponsesDto> withResponsesJson;

    private final JacksonTester<ItemRequestCreateDto> createJson;

    @Test
    public void serializeItemRequestDto() throws Exception {
        ItemRequestDto dto = new ItemRequestDto(1L, "Need a drill", 7L,
                LocalDateTime.of(2026, 3, 10, 9, 30, 0));

        JsonContent<ItemRequestDto> json = requestJson.write(dto);

        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json).extractingJsonPathStringValue("$.description").isEqualTo("Need a drill");
        assertThat(json).extractingJsonPathNumberValue("$.requestorId").isEqualTo(7);
        assertThat(json).extractingJsonPathStringValue("$.created").isEqualTo("2026-03-10T09:30:00");
    }

    @Test
    public void serializeWithResponsesDto_defaultsItemsToEmpty() throws Exception {
        ItemRequestWithResponsesDto dto = new ItemRequestWithResponsesDto(
                1L, "desc", LocalDateTime.of(2026, 3, 10, 9, 30, 0), null);

        var json = withResponsesJson.write(dto);

        assertThat(json).extractingJsonPathArrayValue("$.items").isEmpty();
    }

    @Test
    public void serializeWithResponsesDto_withItems() throws Exception {
        ItemRequestResponseDto resp = new ItemRequestResponseDto(2L, "Drill", 5L);
        ItemRequestWithResponsesDto dto = new ItemRequestWithResponsesDto(
                1L, "desc", LocalDateTime.of(2026, 3, 10, 9, 30, 0), List.of(resp));

        var json = withResponsesJson.write(dto);

        assertThat(json).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(2);
        assertThat(json).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Drill");
        assertThat(json).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(5);
    }

    @Test
    public void deserializeCreateDto() throws Exception {
        ItemRequestCreateDto dto = createJson.parseObject("{\"description\":\"Need\"}");

        assertThat(dto.description()).isEqualTo("Need");
    }
}
