package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingJsonTest {

    @Autowired
    private JacksonTester<BookingDto> bookingDtoJson;

    @Autowired
    private JacksonTester<BookingResponseDto> bookingResponseDtoJson;

    @Test
    void BookingDtoTest() throws Exception {
        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .itemId(2L)
                .bookerId(3L)
                .build();

        JsonContent<BookingDto> result = bookingDtoJson.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(2);
        assertThat(result).extractingJsonPathNumberValue("$.bookerId").isEqualTo(3);
    }

    @Test
    void BookingResponseDtoTest() throws Exception {
        UserDto bookerDto = UserDto.builder()
                .id(1L)
                .name("Dima")
                .email("dima.bill@mail.com")
                .build();

        ItemDto itemDto = ItemDto.builder()
                .id(2L)
                .build();

        BookingResponseDto bookingResponseDto = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(itemDto)
                .booker(bookerDto)
                .status(BookingStatus.WAITING)
                .build();

        JsonContent<BookingResponseDto>  result = bookingResponseDtoJson.write(bookingResponseDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(2);
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo("Dima");
        assertThat(result).extractingJsonPathStringValue("$.booker.email")
                .isEqualTo("dima.bill@mail.com");
    }
}