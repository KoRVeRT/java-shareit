package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private Pageable pageable;

    private Booking booking;
    private BookingDto bookingDto;
    private BookingResponseDto bookingResponseDto;
    private BookingResponseDto bookingResponseDtoForUpdate;
    private BookingDto bookingDtoForUpdate;
    private User booker;
    private User owner;
    private Item item;
    private List<Booking> bookings;

    @BeforeEach
    public void setUp() {
        booker = new User();
        booker.setId(1L);

        owner = new User();
        owner.setId(2L);

        item = new Item();
        item.setAvailable(true);
        item.setId(1L);
        item.setOwner(owner);

        bookings = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Booking booking = Booking.builder()
                    .id((long) i)
                    .item(item)
                    .booker(booker)
                    .status(BookingStatus.WAITING)
                    .build();
            bookings.add(booking);
        }

        booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusMinutes(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        bookingDto = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusMinutes(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
                .bookerId(1L)
                .build();

        bookingResponseDto = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build();

        bookingDtoForUpdate = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusMinutes(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
                .bookerId(owner.getId())
                .approved(true)
                .build();

        bookingResponseDtoForUpdate = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusMinutes(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.APPROVED)
                .build();
    }

    @Test
    void createBooking_whenBookingValid() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toBooking(any(BookingDto.class))).thenReturn(booking);
        when(bookingMapper.toBookingResponseDto(any(Booking.class))).thenReturn(bookingResponseDto);

        BookingResponseDto result = bookingService.createBooking(bookingDto);

        assertEquals(bookingResponseDto, result);
    }

    @Test
    void createBooking_throwsException_whenItemNotAvailable() {
        item.setAvailable(false);

        when(bookingMapper.toBooking(any(BookingDto.class))).thenReturn(booking);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));

        assertThrows(ValidationException.class, () -> bookingService.createBooking(bookingDto));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_throwsException_whenOwnerTriesToBookOwnItem() {
        when(bookingMapper.toBooking(any(BookingDto.class))).thenReturn(booking);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(bookingDto));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_throwsException_whenStartTimeIsLaterThanEndTime() {
        booking.setStart(LocalDateTime.now().plusMonths(1));

        when(bookingMapper.toBooking(any(BookingDto.class))).thenReturn(booking);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));

        assertThrows(ValidationException.class, () -> bookingService.createBooking(bookingDto));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

//    @Test
//    void createBooking_throwsException_whenStartTimeEqualsEndTime() {
//        booking.setStart(LocalDateTime.now());
//        booking.setEnd(LocalDateTime.now());
//
//        when(bookingMapper.toBooking(any(BookingDto.class))).thenReturn(booking);
//        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
//        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
//
//        assertThrows(ValidationException.class, () -> bookingService.createBooking(bookingDto));
//        verify(bookingRepository, never()).save(any(Booking.class));
//    }

    @Test
    void getBookingById_whenBookingFound() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingMapper.toBookingResponseDto(any(Booking.class))).thenReturn(bookingResponseDto);

        BookingResponseDto result = bookingService.getBookingById(booker.getId(), booking.getId());

        assertEquals(bookingResponseDto, result);
    }

    @Test
    void getBookingById_whenBookingNotFound() {
        when(bookingRepository.findById(anyLong())).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(1L, 1L));
    }

    @Test
    void getBookingById_whenUserNotFound() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.ofNullable(booking));
        when(userRepository.findById(anyLong())).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(1L, 1L));
    }

    @Test
    void getBookingById_throwsException_whenBookingRequestedByUnauthorizedUser() {
        User unauthorizedUser = User.builder()
                .id(3L)
                .name("thief")
                .build();
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.ofNullable(booking));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(unauthorizedUser));

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(1L, 1L));
    }

    @Test
    void updateBooking_whenBookingValid() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toBookingResponseDto(any(Booking.class))).thenReturn(bookingResponseDtoForUpdate);

        BookingResponseDto result = bookingService.updateBooking(bookingDtoForUpdate);

        assertEquals(bookingResponseDtoForUpdate, result);
    }

    @Test
    void updateBooking_throwsException_whenBookingNotFound() {
        when(bookingRepository.findById(anyLong())).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> bookingService.updateBooking(bookingDto));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void updateBooking_throwsException_whenBookerIsNotUpdatingOwnBooking() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService.updateBooking(bookingDto));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void updateBooking_throwsException_whenUserIsNotOwnItem() {
        bookingDto.setBookerId(3L);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        assertThrows(ValidationException.class, () -> bookingService.updateBooking(bookingDto));
    }

    @Test
    void updateBooking_throwsException_whenBookingIsNotWaitingStatus() {
        booking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class, () -> bookingService.updateBooking(bookingDtoForUpdate));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void getAllBookingByUserId_whenOwnerIsExist() {
        Page<Booking> page = new PageImpl<>(bookings);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(bookingMapper.toBookingResponseDto(any(Booking.class))).thenReturn(bookingResponseDto);

        List<BookingResponseDto> results = bookingService.getAllBookingByUserId(booker.getId(),
                BookingState.ALL, pageable);

        assertEquals(bookings.size(), results.size());
        verify(bookingMapper, times(bookings.size())).toBookingResponseDto(any(Booking.class));
    }

    @Test
    void getAllBookingByUserId_whenOwnerIsNotExist() {
        when(userRepository.findById(anyLong())).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> bookingService.getAllBookingByOwnerId(1L, BookingState.ALL,
                pageable));
        verify(bookingRepository, never()).findAll(any(Specification.class));
    }
}