package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.repository.BookingSpecifications;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingMapper bookingMapper;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingDto bookingDto) {
        Booking booking = bookingMapper.toBooking(bookingDto);
        Item item = findItemById(bookingDto.getItemId());
        User user = findUserById(bookingDto.getBookerId());
        booking.setBooker(user);
        booking.setItem(item);
        validateCreateBooking(booking);
        booking = bookingRepository.save(booking);
        log.info("Created booking with id:{}", booking.getId());
        return bookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    @Modifying
    @Transactional
    public BookingResponseDto updateBooking(BookingDto bookingDto) {
        Booking booking = findBookingById(bookingDto.getId());
        validateUpdateBooking(booking, bookingDto);
        booking.setStatus(bookingDto.isApproved() ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        log.info("Updated booking with id:{}", booking.getId());
        return bookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto getBookingById(long userId, long bookingId) {
        Booking booking = findBookingById(bookingId);
        User user = findUserById(userId);
        if (!booking.getBooker().getId().equals(user.getId())
                && !booking.getItem().getOwner().getId().equals(user.getId())) {
            throw new NotFoundException(String.format("Booking with id:%d information can only be requested by owner " +
                    "of item, or user who created booking.", booking.getId()));
        }
        log.info("Getting booking with id:{} for user with id:{}", bookingId, userId);
        return bookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getAllBookingByUserId(long bookerId, BookingState bookingState) {
        findUserById(bookerId);
        LocalDateTime currentTime = LocalDateTime.now();
        List<Booking> bookings;
        switch (bookingState) {
            case CURRENT:
                Specification<Booking> specCurrent = BookingSpecifications
                        .findAllBookingsByBookerIdAndStartIsBeforeAndEndIsAfter(bookerId, currentTime);
                bookings = bookingRepository.findAll(specCurrent);
                break;
            case PAST:
                Specification<Booking> specPast = BookingSpecifications
                        .findAllBookingsByBookerIdAndEndIsBeforeOrderByEndDesc(bookerId, currentTime);
                bookings = bookingRepository.findAll(specPast);
                break;
            case FUTURE:
                Specification<Booking> specFuture = BookingSpecifications
                        .findAllBookingsByBookerIdAndStartIsAfterOrderByStartDesc(bookerId, currentTime);
                bookings = bookingRepository.findAll(specFuture);
                break;
            case WAITING:
                Specification<Booking> specWait = BookingSpecifications
                        .findAllBookingsByBookerIdAndStatusEquals(bookerId, BookingStatus.WAITING);
                bookings = bookingRepository.findAll(specWait);
                break;
            case REJECTED:
                Specification<Booking> specReject = BookingSpecifications
                        .findAllBookingsByBookerIdAndStatusEquals(bookerId, BookingStatus.REJECTED);
                bookings = bookingRepository.findAll(specReject);
                break;
            case ALL:
                Specification<Booking> specAll = BookingSpecifications.findAllBookingsByBookerIdOrderByStartDesc(bookerId);
                bookings = bookingRepository.findAll(specAll);
                break;
            default:
                throw new ConflictException("Unknown state: UNSUPPORTED_STATUS");
        }
        log.info("Number of booking in the list = {}", bookings.size());
        return bookings.stream().map(bookingMapper::toBookingResponseDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getAllBookingByOwnerId(long ownerId, BookingState bookingState) {
        findUserById(ownerId);
        LocalDateTime currentTime = LocalDateTime.now();
        List<Booking> bookings;
        switch (bookingState) {
            case CURRENT:
                Specification<Booking> specCurrent = BookingSpecifications
                        .findAllBookingsByItemOwnerIdAndStartIsBeforeAndEndIsAfter(ownerId, currentTime);
                bookings = bookingRepository.findAll(specCurrent);
                break;
            case PAST:
                Specification<Booking> specPast = BookingSpecifications
                        .findAllBookingsByItemOwnerIdAndEndIsBeforeOrderByEndDesc(ownerId, currentTime);
                bookings = bookingRepository.findAll(specPast);
                break;
            case FUTURE:
                Specification<Booking> specFuture = BookingSpecifications
                    .findAllBookingsByItemOwnerIdAndStartIsAfterOrderByStartDesc(ownerId, currentTime);
                bookings = bookingRepository.findAll(specFuture);
                break;
            case WAITING:
                Specification<Booking> specWait = BookingSpecifications
                        .findAllBookingsByItemOwnerIdAndStatusEquals(ownerId, BookingStatus.WAITING);
                bookings = bookingRepository.findAll(specWait);
                break;
            case REJECTED:
                Specification<Booking> specReject = BookingSpecifications
                        .findAllBookingsByItemOwnerIdAndStatusEquals(ownerId, BookingStatus.REJECTED);
                bookings = bookingRepository.findAll(specReject);
                break;
            case ALL:
                Specification<Booking> specAll = BookingSpecifications
                        .findAllBookingsByItemOwnerIdOrderByStartDesc(ownerId);
                bookings = bookingRepository.findAll(specAll);
                break;
            default:
                throw new ConflictException("Unknown state: UNSUPPORTED_STATUS");
        }
        log.info("Number of booking in the list = {}", bookings.size());
        return bookings.stream().map(bookingMapper::toBookingResponseDto).collect(Collectors.toList());
    }

    private void validateCreateBooking(Booking booking) {
        if (Boolean.FALSE.equals(booking.getItem().getAvailable())) {
            throw new ValidationException(String.format("Item with id:%d isn't available", booking.getItem().getId()));
        }
        if (booking.getBooker().getId().equals(booking.getItem().getOwner().getId())) {
            throw new NotFoundException(String.format("Owner with id:%d cannot book his item with id:%d.",
                    booking.getBooker().getId(), booking.getItem().getOwner().getId()));
        }
        if (booking.getStart().isAfter(booking.getEnd())) {
            throw new ValidationException("Start time cannot be later than end time");
        }

        if (booking.getStart().equals(booking.getEnd())) {
            throw new ValidationException("Start time cannot be equal to end time");
        }

    }

    private void validateUpdateBooking(Booking booking, BookingDto bookingDto) {
        if (booking.getBooker().getId().equals(bookingDto.getBookerId())) {
            throw new NotFoundException(String.format("Booker with id:%d isn't owner of item with id:%d.",
                    bookingDto.getBookerId(), booking.getItem().getId()));
        }
        if (!booking.getItem().getOwner().getId().equals(bookingDto.getBookerId())) {
            throw new ValidationException(String.format("User with id:%d isn't owner of item with id:%d.",
                    bookingDto.getBookerId(), booking.getItem().getId()));
        }
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new ValidationException(String.format("Booking with id:%d not waiting for approval",
                    bookingDto.getId()));
        }
    }

    private User findUserById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id:%d not found", userId)));
    }

    private Item findItemById(long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Item with id:%d not found", itemId)));
    }

    private Booking findBookingById(long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Booking with id:%d not found", bookingId)));
    }
}