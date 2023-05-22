package ru.practicum.shareit.booking.repository;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookingSpecifications {
    private static final String BOOKING_START_DATE_FIELD_NAME = "start";
    private static final String BOOKING_STATUS_FIELD_NAME = "status";

    public static Specification<Booking> existsBookerIdAndItemIdAndEndBefore(
            Long bookerId, Long itemId, LocalDateTime time) {
        return (root, query, builder) -> {
            Predicate bookerIdPredicate = builder.equal(root.get("booker").get("id"), bookerId);
            Predicate itemIdPredicate = builder.equal(root.get("item").get("id"), itemId);
            Predicate endPredicate = builder.lessThan(root.get("end"), time);
            Predicate statusPredicate = builder.equal(root.get(BOOKING_STATUS_FIELD_NAME), BookingStatus.APPROVED);
            return builder.and(bookerIdPredicate, itemIdPredicate, endPredicate, statusPredicate);
        };
    }

    public static Specification<Booking> findFirstByItemIdAndStartBeforeOrderByStartDesc(
            Long itemId, LocalDateTime time) {
        return (root, query, builder) -> {
            Predicate itemIdPredicate = builder.equal(root.get("item").get("id"), itemId);
            Predicate startPredicate = builder.lessThan(root.get(BOOKING_START_DATE_FIELD_NAME), time);
            Predicate statusPredicate = builder.notEqual(root.get(BOOKING_STATUS_FIELD_NAME), BookingStatus.REJECTED);
            query.orderBy(builder.desc(root.get(BOOKING_START_DATE_FIELD_NAME)));
            return builder.and(itemIdPredicate, startPredicate, statusPredicate);
        };
    }

    public static Specification<Booking> findFirstByItemIdAndStartAfterOrderByStart(
            Long itemId, LocalDateTime time) {
        return (root, query, builder) -> {
            Predicate itemIdPredicate = builder.equal(root.get("item").get("id"), itemId);
            Predicate startPredicate = builder.greaterThan(root.get(BOOKING_START_DATE_FIELD_NAME), time);
            Predicate statusPredicate = builder.equal(root.get(BOOKING_STATUS_FIELD_NAME), BookingStatus.APPROVED);
            query.orderBy(builder.asc(root.get(BOOKING_START_DATE_FIELD_NAME)));
            return builder.and(itemIdPredicate, startPredicate, statusPredicate);
        };
    }
}