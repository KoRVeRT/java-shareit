package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestPropertySource(properties = {"db.name=testBooking"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookingServiceIntegrationTest {
    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    @BeforeAll
    void setup() {
        LongStream.rangeClosed(1, 10)
                .mapToObj((i) -> UserDto.builder()
                        .name(String.format("User%d", i))
                        .email(String.format("email%d@mail.net", i))
                        .build()).forEach(userService::createUser);

        LongStream.rangeClosed(1, 10)
                .mapToObj((i) -> ItemDto.builder()
                        .ownerId(i)
                        .available(true)
                        .name(String.format("Item%d", i))
                        .description(String.format("Item%d description", i))
                        .build())
                .forEach(itemService::createItem);
    }

    @Test
    void createBooking() {
        BookingDto bookingDto1 = BookingDto.builder()
                .bookerId(2L)
                .itemId(1L)
                .start(LocalDateTime.now().plusMinutes(20).withNano(0))
                .end(LocalDateTime.now().plusMinutes(40).withNano(0))
                .build();

        BookingDto bookingDto2 = BookingDto.builder()
                .bookerId(3L)
                .itemId(2L)
                .start(LocalDateTime.now().plusSeconds(1).withNano(0))
                .end(LocalDateTime.now().plusMinutes(30).withNano(0))
                .build();

        BookingDto actual = bookingService.createBooking(bookingDto1);
        assertEquals(1L, actual.getId(), "Expected booking id to be 1");
        assertEquals(2L, actual.getBooker().getId(), "Expected booker id to be 2");
        assertEquals(1L, actual.getItem().getId(), "Expected item id to be 1");
        assertEquals(BookingStatus.WAITING, actual.getStatus(), "Expected booking status to be WAITING");

        bookingDto2.setBookerId(100L);
        assertThrows(NotFoundException.class, () -> bookingService.createBooking(bookingDto2),
                "Expected NotFoundException when using non-existent user id");
        bookingDto2.setBookerId(3L);

        bookingDto2.setItemId(100L);
        assertThrows(NotFoundException.class, () -> bookingService.createBooking(bookingDto2),
                "Expected NotFoundException when using non-existent item id");
        bookingDto2.setItemId(2L);

        bookingDto2.setStart(bookingDto2.getEnd().plusSeconds(30));
        assertThrows(ValidationException.class, () -> bookingService.createBooking(bookingDto2),
                "Expected ValidationException when end time is before start time");

        long itemId = 2L;
        long ownerItemId = 2L;
        long bookerId = 3L;
        ItemDto itemDto = ItemDto.builder().id(itemId).ownerId(ownerItemId).available(false).build();
        itemService.updateItem(itemDto);
        assertFalse(itemService.getItemById(itemId, bookerId).getAvailable(),
                "Expected item availability to be false");
        assertThrows(ValidationException.class, () -> bookingService.createBooking(bookingDto2),
                "Expected ValidationException when item is not available");
    }

    @Test
    void updateBooking() {
        BookingDto bookingDto1 = BookingDto.builder()
                .bookerId(2L)
                .itemId(1L)
                .start(LocalDateTime.now().plusMinutes(20).withNano(0))
                .end(LocalDateTime.now().plusMinutes(40).withNano(0))
                .build();

        BookingDto actual = bookingService.createBooking(bookingDto1);
        long bookingId = actual.getId();
        long ownerId = 1L;

        BookingDto finalBookingDto1 = BookingDto.builder().id(100L).bookerId(ownerId).approved(true).build();
        assertThrows(NotFoundException.class, () -> bookingService.updateBooking(finalBookingDto1),
                "Expected NotFoundException when using non-existent booking id");

        BookingDto finalBookingDto2 = BookingDto.builder().id(bookingId).bookerId(ownerId + 10L).approved(true).build();
        assertThrows(ValidationException.class, () -> bookingService.updateBooking(finalBookingDto2),
                "Expected ValidationException when using different owner's id");

        BookingDto finalBookingDto3 = BookingDto.builder().id(bookingId).bookerId(ownerId).approved(true).build();
        BookingDto updatedBooking = bookingService.updateBooking(finalBookingDto3);
        assertEquals(BookingStatus.APPROVED, updatedBooking.getStatus(),
                "Expected booking status to be APPROVED after update");

        BookingDto finalBookingDto4 = BookingDto.builder().id(bookingId).bookerId(ownerId).approved(false).build();
        assertThrows(ValidationException.class, () -> bookingService.updateBooking(finalBookingDto4),
                "Expected ValidationException when trying to change a booking not in WAITING status");
    }

    @Test
    void getBookingById() {
        BookingDto bookingDto1 = BookingDto.builder()
                .bookerId(2L)
                .itemId(1L)
                .start(LocalDateTime.now().plusMinutes(20).withNano(0))
                .end(LocalDateTime.now().plusMinutes(40).withNano(0))
                .build();

        BookingDto actual = bookingService.createBooking(bookingDto1);
        long bookingId = actual.getId();
        long ownerId = 1L;
        long bookerId = actual.getBooker().getId();

        BookingDto byOwner = bookingService.getBookingById(ownerId, bookingId);
        assertEquals(actual, byOwner, "Expected the returned booking by owner to match the created booking");
        BookingDto byBooker = bookingService.getBookingById(bookerId, bookingId);
        assertEquals(actual, byBooker, "Expected the returned booking by booker to match the created booking");
        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(ownerId + 10, bookerId),
                "Expected NotFoundException when using non-existent owner id");
        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(10L, bookingId),
                "Expected NotFoundException when using different user's id");
    }

    @Test
    void getAllBookingByUserId() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "start"));
        long bookerId = 4L;
        BookingDto bookingDto1 = BookingDto.builder()
                .bookerId(bookerId)
                .itemId(5L)
                .start(LocalDateTime.now().plusMinutes(10).withNano(0))
                .end(LocalDateTime.now().plusMinutes(40).withNano(0))
                .build();

        BookingDto bookingDto2 = BookingDto.builder()
                .bookerId(bookerId)
                .itemId(7L)
                .start(LocalDateTime.now().withNano(0))
                .end(LocalDateTime.now().plusSeconds(1).plusMinutes(30).withNano(0))
                .build();

        BookingDto actual1 = bookingService.createBooking(bookingDto1);
        BookingDto actual2 = bookingService.createBooking(bookingDto2);

        BookingDto updatedBookingDto1 = BookingDto.builder().id(actual1.getId()).bookerId(actual1.getItem().getId())
                .approved(true).build();
        bookingService.updateBooking(updatedBookingDto1);

        List<BookingDto> allBookingByUserId = bookingService.getAllBookingByUserId(bookerId, BookingState.ALL.toString(),
                pageable);
        assertEquals(2, allBookingByUserId.size(), "Expected to receive 2 bookings for user");
        assertEquals(actual1.getId(), allBookingByUserId.get(0).getId(),
                "Expected first booking to be the one with latest start date");
        assertEquals(1, bookingService.getAllBookingByUserId(bookerId, BookingState.WAITING.toString(),
                pageable).size(), "Expected to find 1 waiting booking for user");
        assertEquals(0, bookingService.getAllBookingByUserId(bookerId, BookingState.REJECTED.toString(),
                pageable).size(), "Expected to find no rejected bookings for user");
        assertEquals(1, bookingService.getAllBookingByUserId(bookerId, BookingState.FUTURE.toString(),
                pageable).size(), "Expected to find 1 future booking for user");
        List<BookingDto> currentBookingByUserId = bookingService.getAllBookingByUserId(bookerId,
                BookingState.CURRENT.toString(), pageable);
        assertEquals(1, bookingService.getAllBookingByUserId(bookerId, BookingState.CURRENT.toString(),
                pageable).size(), "Expected to find 1 current booking for user");
        assertEquals(actual2.getId(), currentBookingByUserId.get(0).getId(),
                "Expected current booking to be the one with earliest start date");
    }

    @Test
    void getAllBookingByOwnerId() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "start"));
        long bookerId = 7L;
        long ownerItemId = 6L;
        BookingDto bookingDto1 = BookingDto.builder()
                .bookerId(bookerId)
                .itemId(6L)
                .start(LocalDateTime.now().plusMinutes(10).withNano(0))
                .end(LocalDateTime.now().plusMinutes(40).withNano(0))
                .build();

        BookingDto bookingDto2 = BookingDto.builder()
                .bookerId(bookerId)
                .itemId(6L)
                .start(LocalDateTime.now().plusMinutes(25).withNano(0))
                .end(LocalDateTime.now().plusDays(5).withNano(0))
                .build();

        BookingDto actual1 = bookingService.createBooking(bookingDto1);
        BookingDto actual2 = bookingService.createBooking(bookingDto2);

        List<BookingDto> allBookingByUserId = bookingService.getAllBookingByOwnerId(ownerItemId,
                BookingState.ALL.toString(),
                pageable);
        assertEquals(2, allBookingByUserId.size(), "Expected to receive 2 bookings for owner");
        assertEquals(actual2.getId(), allBookingByUserId.get(0).getId(),
                "Expected first booking to be the one with latest start date");


        assertEquals(2, bookingService.getAllBookingByOwnerId(ownerItemId, BookingState.WAITING.toString(),
                pageable).size(), "Expected to find 2 waiting bookings for owner");

        BookingDto updatedBookingDto1 = BookingDto.builder().id(actual1.getId()).bookerId(actual1.getItem().getId())
                .approved(false).build();
        bookingService.updateBooking(updatedBookingDto1);
        assertEquals(1, bookingService.getAllBookingByOwnerId(ownerItemId, BookingState.WAITING.toString(),
                pageable).size(), "Expected to find 1 waiting booking for owner after updating booking status");

        assertEquals(1, bookingService.getAllBookingByOwnerId(ownerItemId, BookingState.REJECTED.toString(),
                pageable).size(), "Expected to find 1 rejected booking for owner");

        assertEquals(2, bookingService.getAllBookingByOwnerId(ownerItemId, BookingState.FUTURE.toString(),
                pageable).size(), "Expected to find 2 future bookings for owner");

        assertEquals(0, bookingService.getAllBookingByOwnerId(ownerItemId, BookingState.CURRENT.toString(),
                pageable).size(), "Expected to find no current bookings for owner");
    }
}