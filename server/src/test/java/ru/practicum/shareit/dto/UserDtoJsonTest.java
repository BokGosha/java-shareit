package ru.practicum.shareit.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDtoJsonTest {

    private final JacksonTester<UserDto> userJson;

    private final JacksonTester<UserCreateDto> createJson;

    private final JacksonTester<UserUpdateDto> updateJson;

    @Test
    public void serializeUserDto() throws Exception {
        UserDto dto = new UserDto(1L, "Alice", "a@example.com");

        var json = userJson.write(dto);

        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json).extractingJsonPathStringValue("$.name").isEqualTo("Alice");
        assertThat(json).extractingJsonPathStringValue("$.email").isEqualTo("a@example.com");
    }

    @Test
    public void deserializeUserCreateDto() throws Exception {
        UserCreateDto dto = createJson.parseObject("{\"name\":\"Bob\",\"email\":\"b@example.com\"}");

        assertThat(dto.name()).isEqualTo("Bob");
        assertThat(dto.email()).isEqualTo("b@example.com");
    }

    @Test
    public void deserializeUserUpdateDto_partial() throws Exception {
        UserUpdateDto dto = updateJson.parseObject("{\"email\":\"new@example.com\"}");

        assertThat(dto.name()).isNull();
        assertThat(dto.email()).isEqualTo("new@example.com");
    }
}
