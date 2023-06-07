package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemMapperTest {

    @Autowired
    private JacksonTester<ItemDto> jsonItemDto;

    @Autowired
    private JacksonTester<Item> jsonItem;

    @Test
    void toItemDtoJson() throws Exception {
        List<CommentDto> comments = Arrays.asList(
                new CommentDto(1L, "Test comment 1", "Author", LocalDateTime.now()),
                new CommentDto(2L, "Test comment 2", "Author", LocalDateTime.now())
        );

        BookingDto lastBooking = BookingDto.builder()
                .id(1L)
                .itemId(1L)
                .bookerId(1L)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().plusDays(1))
                .build();
        BookingDto nextBooking = BookingDto.builder()
                .id(2L)
                .itemId(1L)
                .bookerId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(6))
                .build();

        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .ownerId(1L)
                .requestId(1L)
                .comments(comments)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .build();

        JsonContent<ItemDto> result = jsonItemDto.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Test Item");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Test Description");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(result).extractingJsonPathNumberValue("$.ownerId").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(1);
        assertThat(result).extractingJsonPathArrayValue("$.comments").hasSize(2);
        assertThat(result).extractingJsonPathStringValue("$.comments[0].text").isEqualTo("Test comment 1");
        assertThat(result).extractingJsonPathStringValue("$.comments[0].authorName").isEqualTo("Author");
        assertThat(result).extractingJsonPathStringValue("$.comments[1].text").isEqualTo("Test comment 2");
        assertThat(result).extractingJsonPathStringValue("$.comments[1].authorName").isEqualTo("Author");
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.bookerId").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.bookerId").isEqualTo(1);
    }

    @Test
    void toItem() throws IOException {
        Item item = Item.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();

        JsonContent<Item> result = jsonItem.write(item);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Test Item");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Test Description");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
    }
}