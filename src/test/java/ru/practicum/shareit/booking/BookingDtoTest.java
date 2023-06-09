package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoTest {

    @Autowired
    private JacksonTester<BookingDto> bookingDtoJson;

    @Test
    void bookingDtoTest() throws Exception {
        LocalDateTime startTime = LocalDateTime.now().withNano(0);
        LocalDateTime endTime = LocalDateTime.now().plusDays(3).withNano(0);
        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .itemId(2L)
                .start(startTime)
                .end(endTime)
                .bookerId(3L)
                .build();

        JsonContent<BookingDto> result = bookingDtoJson.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(2);
        assertThat(result).extractingJsonPathNumberValue("$.bookerId").isEqualTo(3);
        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo(startTime.toString());
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo(endTime.toString());
    }

    @Test
    void bookingResponseDtoTest() throws Exception {
        UserDto bookerDto = UserDto.builder()
                .id(1L)
                .name("Dima")
                .email("dima.bill@mail.com")
                .build();

        ItemDto itemDto = ItemDto.builder()
                .id(2L)
                .build();

        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime endTime = LocalDateTime.now().plusDays(2).withNano(0);

        BookingDto bookingResponseDto = BookingDto.builder()
                .id(1L)
                .start(startTime)
                .end(endTime)
                .item(itemDto)
                .booker(bookerDto)
                .status(BookingStatus.WAITING)
                .build();

        JsonContent<BookingDto> result = bookingDtoJson.write(bookingResponseDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(startTime.toString());
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(endTime.toString());
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo("Dima");
        assertThat(result).extractingJsonPathStringValue("$.booker.email").isEqualTo("dima.bill@mail.com");
    }
}