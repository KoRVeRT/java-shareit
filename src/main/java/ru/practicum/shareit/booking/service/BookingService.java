package ru.practicum.shareit.booking.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto createBooking(BookingDto bookingDto);

    BookingDto updateBooking(BookingDto bookingDto);

    BookingDto getBookingById(long userId, long bookingId);

    List<BookingDto> getAllBookingByUserId(long userId, String bookingState, Pageable pageable);

    List<BookingDto> getAllBookingByOwnerId(long userId, String bookingState, Pageable pageable);
}