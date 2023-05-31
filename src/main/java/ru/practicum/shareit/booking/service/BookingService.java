package ru.practicum.shareit.booking.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

public interface BookingService {
    BookingResponseDto createBooking(BookingDto bookingDto);

    BookingResponseDto updateBooking(BookingDto bookingDto);

    BookingResponseDto getBookingById(long userId, long bookingId);

    List<BookingResponseDto> getAllBookingByUserId(long userId, BookingState bookingState, Pageable pageable);

    List<BookingResponseDto> getAllBookingByOwnerId(long userId, BookingState bookingState, Pageable pageable);
}