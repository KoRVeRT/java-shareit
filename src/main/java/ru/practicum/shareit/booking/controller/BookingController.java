package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
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
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    public static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto createBooking(
            @RequestHeader(USER_ID_HEADER) long userId,
            @Valid @RequestBody BookingDto bookingDto) {
        bookingDto.setBookerId(userId);
        return bookingService.createBooking(bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto updateBooking(
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
    public BookingResponseDto getBookingById(
            @RequestHeader(USER_ID_HEADER) long userId,
            @PathVariable Long bookingId) {
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingResponseDto> getAllBookingByUserId(
            @RequestHeader(USER_ID_HEADER) long userId,
            @RequestParam(required = false, defaultValue = "ALL") String state) {
        BookingState bookingState = checkBookingState(state);
        return bookingService.getAllBookingByUserId(userId, bookingState);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getAllBookingByOwnerId(
            @RequestHeader(USER_ID_HEADER) long userId,
            @RequestParam(required = false, defaultValue = "ALL") String state) {
        BookingState bookingState = checkBookingState(state);
        return bookingService.getAllBookingByOwnerId(userId, bookingState);
    }

    private BookingState checkBookingState(String state) {
        try {
            return BookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown state: " + state);
        }
    }
}