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
import ru.practicum.shareit.booking.dto.BookingResponseDto;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest()
@TestPropertySource(properties = {"db.name=bookingTest"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookingServiceITest {
    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;
    private BookingDto bookingDto1;
    private BookingDto bookingDto2;

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

        bookingDto1 = BookingDto.builder()
                .bookerId(2L)
                .itemId(1L)
                .start(LocalDateTime.now().plusMinutes(20).withNano(0))
                .end(LocalDateTime.now().plusMinutes(40).withNano(0))
                .build();

        bookingDto2 = BookingDto.builder()
                .bookerId(3L)
                .itemId(2L)
                .start(LocalDateTime.now().plusSeconds(1).withNano(0))
                .end(LocalDateTime.now().plusMinutes(30).withNano(0))
                .build();
    }

    @Test
    void createBooking() {
        BookingResponseDto actual = bookingService.createBooking(bookingDto1);
        assertEquals(1L, actual.getId());
        assertEquals(2L, actual.getBooker().getId());
        assertEquals(1L, actual.getItem().getId());
        assertEquals(BookingStatus.WAITING, actual.getStatus());

        //user not exists
        bookingDto2.setBookerId(100L);
        assertThrows(NotFoundException.class, () -> bookingService.createBooking(bookingDto2));
        bookingDto2.setBookerId(3L);

        //item not exists
        bookingDto2.setItemId(100L);
        assertThrows(NotFoundException.class, () -> bookingService.createBooking(bookingDto2));
        bookingDto2.setItemId(2L);

        //end before start
        bookingDto2.setStart(bookingDto2.getEnd().plusSeconds(30));
        assertThrows(ValidationException.class, () -> bookingService.createBooking(bookingDto2));

        //item not available
        long itemId = 2L;
        long ownerItemId = 2L;
        long bookerId = 3L;
        ItemDto itemDto = ItemDto.builder().id(itemId).ownerId(ownerItemId).available(false).build();
        itemService.updateItem(itemDto);
        assertFalse(itemService.getItemById(itemId, bookerId).getAvailable());
        assertThrows(ValidationException.class, () -> bookingService.createBooking(bookingDto2));
    }

    @Test
    void updateBooking() {
        BookingResponseDto actual = bookingService.createBooking(bookingDto1);
        long bookingId = actual.getId();
        long ownerId = 1L;

        //booking not exists
        BookingDto finalBookingDto1 = BookingDto.builder().id(100L).bookerId(ownerId).approved(true).build();
        assertThrows(NotFoundException.class, () -> bookingService.updateBooking(finalBookingDto1));

        //booking of other owner
        BookingDto finalBookingDto2 = BookingDto.builder().id(bookingId).bookerId(ownerId + 10L).approved(true).build();
        assertThrows(ValidationException.class, () -> bookingService.updateBooking(finalBookingDto2));

        //accept updated
        BookingDto finalBookingDto3 = BookingDto.builder().id(bookingId).bookerId(ownerId).approved(true).build();
        BookingResponseDto updatedBooking = bookingService.updateBooking(finalBookingDto3);
        assertEquals(BookingStatus.APPROVED, updatedBooking.getStatus());

        //change a booking not in waiting status
        BookingDto finalBookingDto4 = BookingDto.builder().id(bookingId).bookerId(ownerId).approved(false).build();
        assertThrows(ValidationException.class, () -> bookingService.updateBooking(finalBookingDto4));
    }

    @Test
    void getBookingById() {
        BookingResponseDto actual = bookingService.createBooking(bookingDto1);
        long bookingId = actual.getId();
        long ownerId = 1L;
        long bookerId = actual.getBooker().getId();

        //get owner
        BookingResponseDto byOwner = bookingService.getBookingById(ownerId, bookingId);
        assertEquals(actual, byOwner);

        //get booker
        BookingResponseDto byBooker = bookingService.getBookingById(bookerId, bookingId);
        assertEquals(actual, byBooker);

        //get booking not exists
        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(ownerId + 10, bookerId));

        //get another user
        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(10L, bookingId));
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

        BookingResponseDto actual1 = bookingService.createBooking(bookingDto1);
        BookingResponseDto actual2 = bookingService.createBooking(bookingDto2);

        BookingDto updatedBookingDto1 = BookingDto.builder().id(actual1.getId()).bookerId(actual1.getItem().getId())
                .approved(true).build();
        bookingService.updateBooking(updatedBookingDto1);

        //get ALL = 2
        List<BookingResponseDto> allBookingByUserId = bookingService.getAllBookingByUserId(bookerId, BookingState.ALL,
                pageable);
        assertEquals(2, allBookingByUserId.size());
        //check sort
        assertEquals(actual1.getId(), allBookingByUserId.get(0).getId());

        //get WAITING = 1
        assertEquals(1, bookingService.getAllBookingByUserId(bookerId, BookingState.WAITING, pageable).size());

        //get REJECTED = 0
        assertEquals(0, bookingService.getAllBookingByUserId(bookerId, BookingState.REJECTED, pageable).size());

        //get FUTURE = 1
        assertEquals(1, bookingService.getAllBookingByUserId(bookerId, BookingState.FUTURE, pageable).size());

        //get CURRENT = 1
        List<BookingResponseDto> currentBookingByUserId = bookingService.getAllBookingByUserId(bookerId,
                BookingState.CURRENT, pageable);
        assertEquals(1, bookingService.getAllBookingByUserId(bookerId, BookingState.CURRENT, pageable).size());
        //check bookingId
        assertEquals(actual2.getId(), currentBookingByUserId.get(0).getId());
    }

    @Test
    void getAllBookingByOwnerId() throws InterruptedException {
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
                .start(LocalDateTime.now().withNano(0))
                .end(LocalDateTime.now().plusSeconds(1).withNano(0))
                .build();

        BookingResponseDto actual1 = bookingService.createBooking(bookingDto1);
        BookingResponseDto actual2 = bookingService.createBooking(bookingDto2);

        //get ALL = 2
        List<BookingResponseDto> allBookingByUserId = bookingService.getAllBookingByOwnerId(ownerItemId, BookingState.ALL,
                pageable);
        assertEquals(2, allBookingByUserId.size());
        //check sort
        assertEquals(actual1.getId(), allBookingByUserId.get(0).getId());

        //get WAITING = 2
        assertEquals(2, bookingService.getAllBookingByOwnerId(ownerItemId, BookingState.WAITING, pageable).size());
        //check status WAITING after updateBooking
        BookingDto updatedBookingDto1 = BookingDto.builder().id(actual1.getId()).bookerId(actual1.getItem().getId())
                .approved(false).build();
        bookingService.updateBooking(updatedBookingDto1);
        assertEquals(1, bookingService.getAllBookingByOwnerId(ownerItemId, BookingState.WAITING, pageable).size());

        //get REJECTED = 1
        assertEquals(1, bookingService.getAllBookingByOwnerId(ownerItemId, BookingState.REJECTED, pageable).size());

        //get FUTURE = 1
        assertEquals(1, bookingService.getAllBookingByOwnerId(ownerItemId, BookingState.FUTURE, pageable).size());

        //get CURRENT = 1
        List<BookingResponseDto> currentBookingByUserId = bookingService.getAllBookingByOwnerId(ownerItemId,
                BookingState.CURRENT, pageable);
        assertEquals(1, bookingService.getAllBookingByOwnerId(ownerItemId, BookingState.CURRENT, pageable).size());
        //check bookingId
        assertEquals(actual2.getId(), currentBookingByUserId.get(0).getId());
        //check CURRENT after finish booking time
        Thread.sleep(1000);
        assertTrue(bookingService.getAllBookingByOwnerId(ownerItemId, BookingState.CURRENT, pageable).isEmpty());
    }
}