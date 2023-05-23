package ru.practicum.shareit.booking.model;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public enum BookingState {
    ALL((r, q, cb) -> cb.isTrue(cb.literal(true))),
    CURRENT((r, q, cb) -> cb.between(
            cb.literal(LocalDateTime.now()), r.<LocalDateTime>get("start"), r.<LocalDateTime>get("end"))),
    PAST((r, q, cb) -> cb.lessThan(r.<LocalDateTime>get("end"), LocalDateTime.now())),
    FUTURE((r, q, cb) -> cb.greaterThan(r.<LocalDateTime>get("start"), LocalDateTime.now())),
    WAITING((r, q, cb) -> cb.equal(r.<BookingStatus>get("status"), BookingStatus.WAITING)),
    REJECTED((r, q, cb) -> cb.equal(r.<BookingStatus>get("status"), BookingStatus.REJECTED));

    private final Specification<Booking> specification;

    private BookingState(Specification<Booking> specification) {
        this.specification = specification;
    }

    public Specification<Booking> getSpecification() {
        return specification;
    }
}