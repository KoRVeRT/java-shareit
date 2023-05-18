package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllBookingsByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findAllBookingsByBookerIdAndStartIsBeforeAndEndIsAfter(
            Long bookerId, LocalDateTime starTime, LocalDateTime endTime);

    List<Booking> findAllBookingsByBookerIdAndEndIsBeforeOrderByEndDesc(Long bookerId, LocalDateTime endTime);

    List<Booking> findAllBookingsByBookerIdAndStartIsAfterOrderByStartDesc(Long bookerId, LocalDateTime startTime);

    List<Booking> findAllBookingsByBookerIdAndStatusEquals(Long bookerId, BookingStatus status);

    List<Booking> findAllBookingsByItemOwnerIdOrderByStartDesc(Long ownerId);

    List<Booking> findAllBookingsByItemOwnerIdAndStartIsBeforeAndEndIsAfter(
            Long ownerId, LocalDateTime startTime, LocalDateTime endTime);

    List<Booking> findAllBookingsByItemOwnerIdAndEndIsBeforeOrderByEndDesc(Long ownerId, LocalDateTime endTime);

    List<Booking> findAllBookingsByItemOwnerIdAndStartIsAfterOrderByStartDesc(Long ownerId, LocalDateTime startTime);

    List<Booking> findAllBookingsByItemOwnerIdAndStatusEquals(Long ownerId, BookingStatus status);

    Optional<Booking> findFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(long itemId, BookingStatus status,
                                                                               LocalDateTime now);

    Optional<Booking> findFirstByItemIdAndStatusAndStartAfterOrderByStart(long itemId, BookingStatus status,
                                                                          LocalDateTime now);

    boolean existsByBookerIdAndItemIdAndEndIsBeforeAndStatus(Long bookerId, Long itemId, LocalDateTime endTime,
                                                             BookingStatus status);
}