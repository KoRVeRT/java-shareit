package ru.practicum.shareit.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.ValidationException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErrorHandlerTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserService userService;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void getUserNotFoundTest() {
        long userId = 1L;

        when(userService.getUserById(userId)).thenThrow(new NotFoundException("User not found"));

        assertThrows(NotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    void createBooking_ItemNotAvailable_ThrowsValidationExceptionTest() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setBookerId(1L);

        Item item = new Item();
        item.setAvailable(false);

        User user = new User();

        Booking booking = new Booking();

        when(bookingMapper.toBooking(bookingDto)).thenReturn(booking);
        when(userRepository.findById(bookingDto.getBookerId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(bookingDto.getItemId())).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> {
            bookingService.createBooking(bookingDto);
        });
    }
}
