package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class BookingStateTest {

    @Test
    void getSpecification_whenStatusAll() {
        BookingState bookingState = BookingState.ALL;
        Specification<Booking> specification = bookingState.getSpecification();

        assertNotNull(specification);
    }

    @Test
    void getSpecification_whenStatusCurrent() {
        BookingState bookingState = BookingState.CURRENT;
        Specification<Booking> specification = bookingState.getSpecification();

        assertNotNull(specification);
    }

    @Test
    void getSpecification_whenStatusPast() {
        BookingState bookingState = BookingState.PAST;
        Specification<Booking> specification = bookingState.getSpecification();

        assertNotNull(specification);
    }

    @Test
    void getSpecification_whenStatusFuture() {
        BookingState bookingState = BookingState.FUTURE;
        Specification<Booking> specification = bookingState.getSpecification();

        assertNotNull(specification);
    }

    @Test
    void getSpecification_whenStatusWaiting() {
        BookingState bookingState = BookingState.WAITING;
        Specification<Booking> specification = bookingState.getSpecification();

        assertNotNull(specification);
    }


    @Test
    void getSpecification_whenStatusReject() {
        BookingState bookingState = BookingState.REJECTED;
        Specification<Booking> specification = bookingState.getSpecification();

        assertNotNull(specification);
    }
}
