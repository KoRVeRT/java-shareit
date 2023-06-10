package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    public static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private static final String BOOKING_START_DATE_FIELD_NAME = "start";
    private final BookingService bookingService;

    @PostMapping
    public BookingDto createBooking(
            @RequestHeader(USER_ID_HEADER) long userId,
            @RequestBody BookingDto bookingDto) {
        bookingDto.setBookerId(userId);
        return bookingService.createBooking(bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto updateBooking(
            @RequestHeader(USER_ID_HEADER) long userId,
            @RequestParam(required = false) boolean approved,
            @PathVariable long bookingId) {

        BookingDto bookingDto = BookingDto.builder()
                .id(bookingId)
                .approved(approved)
                .bookerId(userId)
                .build();
        return bookingService.updateBooking(bookingDto);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(
            @RequestHeader(USER_ID_HEADER) long userId,
            @PathVariable Long bookingId) {
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getAllBookingByUserId(
            @RequestHeader(USER_ID_HEADER) long userId,
            @RequestParam(required = false, defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(from / size, size,
                Sort.by(Sort.Direction.DESC, BOOKING_START_DATE_FIELD_NAME));
        return bookingService.getAllBookingByUserId(userId, state, pageable);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllBookingByOwnerId(
            @RequestHeader(USER_ID_HEADER) long userId,
            @RequestParam(required = false, defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(from / size, size,
                Sort.by(Sort.Direction.DESC, BOOKING_START_DATE_FIELD_NAME));
        return bookingService.getAllBookingByOwnerId(userId, state, pageable);
    }
}