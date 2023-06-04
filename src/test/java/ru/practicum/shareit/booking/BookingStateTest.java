package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class BookingStateTest {

    @Test
    void etSpecificationTest() {
        BookingState bookingState = BookingState.ALL;
        Specification<Booking> specification = bookingState.getSpecification();

        assertNotNull(specification);
    }
}
