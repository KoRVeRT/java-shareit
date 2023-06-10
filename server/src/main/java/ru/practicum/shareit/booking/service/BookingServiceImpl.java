package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
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
    public BookingDto createBooking(BookingDto bookingDto) {
        Booking booking = bookingMapper.toBooking(bookingDto);
        Item item = findItemById(bookingDto.getItemId());
        User user = findUserById(bookingDto.getBookerId());
        booking.setBooker(user);
        booking.setItem(item);
        validateCreateBooking(booking);
        booking = bookingRepository.save(booking);
        return bookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    @Modifying
    @Transactional
    public BookingDto updateBooking(BookingDto bookingDto) {
        Booking booking = findBookingById(bookingDto.getId());
        validateUpdateBooking(booking, bookingDto);
        booking.setStatus(bookingDto.isApproved() ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        log.info("Updated booking with id:{}", booking.getId());
        return bookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getBookingById(long userId, long bookingId) {
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
    public List<BookingDto> getAllBookingByUserId(long bookerId, String state, Pageable pageable) {
        BookingState bookingState = checkBookingState(state);
        findUserById(bookerId);
        Specification<Booking> byBookerId = (r, q, cb) -> cb.equal(
                r.<User>get("booker").get("id"), bookerId
        );
        return bookingRepository.findAll(Specification.where(byBookerId).and(bookingState.getSpecification()),
                        pageable).stream()
                .map(bookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getAllBookingByOwnerId(long ownerId, String state, Pageable pageable) {
        BookingState bookingState = checkBookingState(state);
        findUserById(ownerId);
        Specification<Booking> byOwnerId = (r, q, cb) -> cb.equal(
                r.<Item>get("item").<User>get("owner").get("id"), ownerId
        );
        return bookingRepository.findAll(Specification.where(byOwnerId).and(bookingState.getSpecification()),
                        pageable).stream()
                .map(bookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    private void validateCreateBooking(Booking booking) {
        if (booking.getItem().getAvailable() == null || Boolean.FALSE.equals(booking.getItem().getAvailable())) {
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
            throw new NotFoundException(String.format("Booker with id:%d cannot change his own booking with id:%d.",
                    bookingDto.getBookerId(), booking.getId()));
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

    private BookingState checkBookingState(String state) {
        try {
            return BookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown state: " + state);
        }
    }
}