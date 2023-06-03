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
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        item.setId(1L);
        item.setOwner(owner);

        bookings = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Booking booking = new Booking();
            booking.setId((long) i);
            booking.setItem(item);
            booking.setBooker(booker);
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
    }

    @Test
    void createBookingTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toBooking(any(BookingDto.class))).thenReturn(booking);
        when(bookingMapper.toBookingResponseDto(any(Booking.class))).thenReturn(bookingResponseDto);

        BookingResponseDto result = bookingService.createBooking(bookingDto);

        assertEquals(bookingResponseDto, result);
    }

    @Test
    void getBookingByIdTest() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingMapper.toBookingResponseDto(any(Booking.class))).thenReturn(bookingResponseDto);

        BookingResponseDto result = bookingService.getBookingById(booker.getId(), booking.getId());

        assertEquals(bookingResponseDto, result);
    }

    @Test
    void updateBookingTest() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toBookingResponseDto(any(Booking.class))).thenReturn(bookingResponseDto);

        BookingDto bookingDtoForUpdate = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusMinutes(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
                .bookerId(owner.getId())
                .approved(true)
                .build();

        BookingResponseDto result = bookingService.updateBooking(bookingDtoForUpdate);

        assertEquals(bookingResponseDto, result);
    }

    @Test
    void getAllBookingByUserIdTest() {
        Page<Booking> page = new PageImpl<>(bookings);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(bookingMapper.toBookingResponseDto(any(Booking.class))).thenReturn(bookingResponseDto);

        List<BookingResponseDto> results = bookingService.getAllBookingByUserId(booker.getId(),
                BookingState.WAITING, pageable);

        assertEquals(bookings.size(), results.size());
        verify(bookingMapper, times(bookings.size())).toBookingResponseDto(any(Booking.class));
    }

    @Test
    void getAllBookingByOwnerIdTest() {
        Page<Booking> page = new PageImpl<>(bookings);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(bookingRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(bookingMapper.toBookingResponseDto(any(Booking.class))).thenReturn(bookingResponseDto);

        List<BookingResponseDto> results = bookingService.getAllBookingByOwnerId(owner.getId(),
                BookingState.WAITING, pageable);

        assertEquals(bookings.size(), results.size());
        verify(bookingMapper, times(bookings.size())).toBookingResponseDto(any(Booking.class));
    }
}