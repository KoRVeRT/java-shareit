package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingMapperTest {

    @InjectMocks
    private BookingMapper bookingMapper;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private UserMapper userMapper;

    @Test
    void toBookingDto() {
        Item item = new Item();
        item.setId(1L);

        User user = new User();
        user.setId(1L);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.now());
        booking.setEnd(LocalDateTime.now().plusDays(1));
        booking.setItem(item);
        booking.setBooker(user);

        BookingDto bookingDto = bookingMapper.toBookingDto(booking);

        assertEquals(booking.getId(), bookingDto.getId());
        assertEquals(booking.getStart(), bookingDto.getStart());
        assertEquals(booking.getEnd(), bookingDto.getEnd());
        assertEquals(item.getId(), bookingDto.getItemId());
        assertEquals(user.getId(), bookingDto.getBookerId());
    }

    @Test
    void toBookingResponseDto() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.now());
        booking.setEnd(LocalDateTime.now().plusDays(1));
        booking.setStatus(BookingStatus.WAITING);

        when(itemMapper.toItemDto(booking.getItem())).thenReturn(new ItemDto());
        when(userMapper.toUserDto(booking.getBooker())).thenReturn(new UserDto());

        BookingResponseDto bookingResponseDto = bookingMapper.toBookingResponseDto(booking);

        assertEquals(booking.getId(), bookingResponseDto.getId());
        assertEquals(booking.getStart(), bookingResponseDto.getStart());
        assertEquals(booking.getEnd(), bookingResponseDto.getEnd());
        assertEquals(booking.getStatus(), bookingResponseDto.getStatus());
    }

    @Test
    void toBooking() {
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .build();

        Booking booking = bookingMapper.toBooking(bookingDto);

        assertEquals(bookingDto.getStart(), booking.getStart());
        assertEquals(bookingDto.getEnd(), booking.getEnd());
        assertEquals(BookingStatus.WAITING, booking.getStatus());
    }
}
