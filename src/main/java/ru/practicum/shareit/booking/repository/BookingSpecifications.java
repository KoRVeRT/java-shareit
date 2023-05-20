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

    public static Specification<Booking> existsBookerIdAndItemIdAndEndBefore(
            Long bookerId, Long itemId, LocalDateTime time) {
        return (root, query, builder) -> {
            Predicate bookerIdPredicate = builder.equal(root.get("booker").get("id"), bookerId);
            Predicate itemIdPredicate = builder.equal(root.get("item").get("id"), itemId);
            Predicate endPredicate = builder.lessThan(root.get("end"), time);
            Predicate statusPredicate = builder.equal(root.get("status"), BookingStatus.APPROVED);
            return builder.and(bookerIdPredicate, itemIdPredicate, endPredicate, statusPredicate);
        };
    }

    public static Specification<Booking> findFirstByItemIdAndStartBeforeOrderByStartDesc(
            Long itemId, LocalDateTime time) {
        return (root, query, builder) -> {
            Predicate itemIdPredicate = builder.equal(root.get("item").get("id"), itemId);
            Predicate startPredicate = builder.lessThan(root.get("start"), time);
            Predicate statusPredicate = builder.notEqual(root.get("status"), BookingStatus.REJECTED);
            query.orderBy(builder.desc(root.get("start")));
            return builder.and(itemIdPredicate, startPredicate, statusPredicate);
        };
    }

    public static Specification<Booking> findFirstByItemIdAndStartAfterOrderByStart(
            Long itemId, LocalDateTime time) {
        return (root, query, builder) -> {
            Predicate itemIdPredicate = builder.equal(root.get("item").get("id"), itemId);
            Predicate startPredicate = builder.greaterThan(root.get("start"), time);
            Predicate statusPredicate = builder.equal(root.get("status"), BookingStatus.APPROVED);
            query.orderBy(builder.asc(root.get("start")));
            return builder.and(itemIdPredicate, startPredicate, statusPredicate);
        };
    }

    public static Specification<Booking> findAllBookingsByBookerIdAndStatusEquals(
            Long bookerId, BookingStatus status) {
        return (root, query, builder) -> {
            Predicate bookerIdPredicate = builder.equal(root.get("booker").get("id"), bookerId);
            Predicate statusPredicate = builder.equal(root.get("status"), status);
            return builder.and(bookerIdPredicate, statusPredicate);
        };
    }

    public static Specification<Booking> findAllBookingsByItemOwnerIdAndStatusEquals(
            Long ownerId, BookingStatus status) {
        return (root, query, builder) -> {
            Predicate bookerIdPredicate = builder.equal(root.get("item").get("owner").get("id"), ownerId);
            Predicate statusPredicate = builder.equal(root.get("status"), status);
            return builder.and(bookerIdPredicate, statusPredicate);
        };
    }

    public static Specification<Booking> findAllBookingsByBookerIdAndStartIsBeforeAndEndIsAfter(
            Long bookerId, LocalDateTime time) {
        return (root, query, builder) -> {
            Predicate bookerIdPredicate = builder.equal(root.get("booker").get("id"), bookerId);
            Predicate startPredicate = builder.lessThan(root.get("start"), time);
            Predicate endPredicate = builder.greaterThan(root.get("end"), time);
            return builder.and(bookerIdPredicate, startPredicate, endPredicate);
        };
    }

    public static Specification<Booking> findAllBookingsByItemOwnerIdAndStartIsBeforeAndEndIsAfter(
            Long ownerId, LocalDateTime time) {
        return (root, query, builder) -> {
            Predicate bookerIdPredicate = builder.equal(root.get("item").get("owner").get("id"), ownerId);
            Predicate startPredicate = builder.lessThan(root.get("start"), time);
            Predicate endPredicate = builder.greaterThan(root.get("end"), time);
            return builder.and(bookerIdPredicate, startPredicate, endPredicate);
        };
    }

    public static Specification<Booking> findAllBookingsByBookerIdAndStartIsAfterOrderByStartDesc(
            Long bookerId, LocalDateTime time) {
        return (root, query, builder) -> {
            Predicate bookerIdPredicate = builder.equal(root.get("booker").get("id"), bookerId);
            Predicate startPredicate = builder.greaterThan(root.get("start"), time);
            query.orderBy(builder.desc(root.get("start")));
            return builder.and(bookerIdPredicate, startPredicate);
        };
    }

    public static Specification<Booking> findAllBookingsByItemOwnerIdAndStartIsAfterOrderByStartDesc(
            Long ownerId, LocalDateTime time) {
        return (root, query, builder) -> {
            Predicate bookerIdPredicate = builder.equal(root.get("item").get("owner").get("id"), ownerId);
            Predicate startPredicate = builder.greaterThan(root.get("start"), time);
            query.orderBy(builder.desc(root.get("start")));
            return builder.and(bookerIdPredicate, startPredicate);
        };
    }

    public static Specification<Booking> findAllBookingsByItemOwnerIdAndEndIsBeforeOrderByEndDesc(
            Long ownerId, LocalDateTime time) {
        return (root, query, builder) -> {
            Predicate bookerIdPredicate = builder.equal(root.get("item").get("owner").get("id"), ownerId);
            Predicate endPredicate = builder.lessThan(root.get("end"), time);
            query.orderBy(builder.desc(root.get("end")));
            return builder.and(bookerIdPredicate, endPredicate);
        };
    }

    public static Specification<Booking> findAllBookingsByBookerIdAndEndIsBeforeOrderByEndDesc(
            Long bookerId, LocalDateTime time) {
        return (root, query, builder) -> {
            Predicate bookerIdPredicate = builder.equal(root.get("booker").get("id"), bookerId);
            Predicate endPredicate = builder.lessThan(root.get("end"), time);
            query.orderBy(builder.desc(root.get("end")));
            return builder.and(bookerIdPredicate, endPredicate);
        };
    }

    public static Specification<Booking> findAllBookingsByBookerIdOrderByStartDesc(
            Long bookerId) {
        return (root, query, builder) -> {
            Predicate bookerIdPredicate = builder.equal(root.get("booker").get("id"), bookerId);
            query.orderBy(builder.desc(root.get("start")));
            return builder.and(bookerIdPredicate);
        };
    }

    public static Specification<Booking> findAllBookingsByItemOwnerIdOrderByStartDesc(
            Long ownerId) {
        return (root, query, builder) -> {
            Predicate bookerIdPredicate = builder.equal(root.get("item").get("owner").get("id"), ownerId);
            query.orderBy(builder.desc(root.get("start")));
            return builder.and(bookerIdPredicate);
        };
    }
}