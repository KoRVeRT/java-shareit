package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

public interface BookingService {
    BookingResponseDto createBooking(BookingDto bookingDto);

    BookingResponseDto updateBooking(BookingDto bookingDto);

    BookingResponseDto getBookingById(long userId, long bookingId);

    List<BookingResponseDto> getAllBookingByUserId(long userId, BookingState bookingState);

    List<BookingResponseDto> getAllBookingByOwnerId(long userId, BookingState bookingState);
}