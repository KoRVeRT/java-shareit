package ru.practicum.shareit.request;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestMapperTest {

    @Autowired
    private JacksonTester<ItemRequestDto> itemRequestDtoJson;

    @Autowired
    private JacksonTester<ItemRequest> itemRequestJson;

    @Test
    void toItemRequest() throws IOException {
        ItemRequest itemRequest = ItemRequest
                .builder()
                .description("description")
                .build();

        JsonContent<ItemRequest> result = itemRequestJson.write(itemRequest);

        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("description");
    }

    @SneakyThrows
    @Test
    void toItemRequestDto() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("Dima")
                .email("dima.bill@mail.com")
                .build();

        ItemRequestDto itemRequestDto = ItemRequestDto
                .builder()
                .id(1L)
                .description("description")
                .requestor(userDto)
                .created(LocalDateTime.now())
                .build();

        JsonContent<ItemRequestDto> result = itemRequestDtoJson.write(itemRequestDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("description");
        assertThat(result).extractingJsonPathNumberValue("$.requestor.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.requestor.name").isEqualTo("Dima");
        assertThat(result).extractingJsonPathStringValue("$.requestor.email")
                .isEqualTo("dima.bill@mail.com");
    }
}