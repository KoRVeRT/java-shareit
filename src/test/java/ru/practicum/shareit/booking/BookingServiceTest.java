package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Spy
    private BookingMapper bookingMapper = new BookingMapper(new ItemMapper(), new UserMapper());

    @Mock
    private Pageable pageable;

    @Test
    void createBooking_whenBookingValid() {
        LocalDateTime currentTime = LocalDateTime.now();
        BookingDto bookingDto = bookingDto(1, currentTime);

        Booking booking = booking(currentTime);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item()));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user(1)));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        bookingService.createBooking(bookingDto);

        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void createBooking_throwsException_whenItemNotAvailable() {
        LocalDateTime currentTime = LocalDateTime.now();
        BookingDto bookingDto = bookingDto(1, currentTime);
        Item item = item();
        item.setAvailable(false);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user(1)));


        assertThrows(ValidationException.class, () -> bookingService.createBooking(bookingDto));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_throwsException_whenItemNotFound() {
        LocalDateTime currentTime = LocalDateTime.now();
        BookingDto bookingDto = bookingDto(1, currentTime);
        Item item = item();
        item.setAvailable(false);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(bookingDto));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_throwsException_whenUserNotFound() {
        LocalDateTime currentTime = LocalDateTime.now();
        BookingDto bookingDto = bookingDto(1, currentTime);
        Item item = item();
        item.setAvailable(false);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(bookingDto));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_throwsException_whenOwnerTriesToBookOwnItem() {
        LocalDateTime currentTime = LocalDateTime.now();
        BookingDto bookingDto = bookingDto(1, currentTime);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item()));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user(2)));

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(bookingDto));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_throwsException_whenStartTimeIsLaterThanEndTime() {
        LocalDateTime currentTime = LocalDateTime.now();
        BookingDto bookingDto = bookingDto(1, currentTime);
        bookingDto.setStart(currentTime.plusDays(10));

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item()));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user(1)));

        assertThrows(ValidationException.class, () -> bookingService.createBooking(bookingDto));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_throwsException_whenStartTimeEqualsEndTime() {
        LocalDateTime currentTime = LocalDateTime.now();
        BookingDto bookingDto = bookingDto(1, currentTime);
        bookingDto.setStart(currentTime.plusDays(5));
        bookingDto.setEnd(currentTime.plusDays(5));

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item()));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user(1)));

        assertThrows(ValidationException.class, () -> bookingService.createBooking(bookingDto));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void getBookingById_whenBookingFound() {
        LocalDateTime currentTime = LocalDateTime.now();

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking(currentTime)));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user(1)));

        bookingService.getBookingById(1L, booking(currentTime).getId());

        verify(bookingMapper, times(1)).toBookingResponseDto(any(Booking.class));
    }

    @Test
    void getBookingById_whenBookingNotFound() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(1L, 1L));
        verify(bookingMapper, never()).toBookingResponseDto(any(Booking.class));
    }

    @Test
    void getBookingById_whenUserNotFound() {
        LocalDateTime currentTime = LocalDateTime.now();

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking(currentTime)));
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(1L, 1L));
        verify(bookingMapper, never()).toBookingResponseDto(any(Booking.class));
    }

    @Test
    void getBookingById_throwsException_whenBookingRequestedByUnauthorizedUser() {
        LocalDateTime currentTime = LocalDateTime.now();
        User unauthorizedUser = user(3L);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking(currentTime)));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(unauthorizedUser));

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(1L, 1L));
        verify(bookingMapper, never()).toBookingResponseDto(any(Booking.class));
    }

    @Test
    void updateBooking_whenBookingValid() {
        LocalDateTime currentTime = LocalDateTime.now();
        BookingDto bookingDto = bookingDto(2, currentTime);
        bookingDto.setStatus(BookingStatus.APPROVED);
        Booking booking = booking(currentTime);

        when(bookingRepository.findById(booking(currentTime).getId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        bookingService.updateBooking(bookingDto);

        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void updateBooking_throwsException_whenBookingNotFound() {
        BookingDto bookingDto = bookingDto(1, LocalDateTime.now());

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.updateBooking(bookingDto));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void updateBooking_throwsException_whenBookerIsNotUpdatingOwnBooking() {
        LocalDateTime currentTime = LocalDateTime.now();
        BookingDto bookingDto = bookingDto(1, LocalDateTime.now());

        when(bookingRepository.findById(booking(currentTime).getId())).thenReturn(Optional.of(booking(currentTime)));

        assertThrows(NotFoundException.class, () -> bookingService.updateBooking(bookingDto));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void updateBooking_throwsException_whenUserIsNotOwnItem() {
        LocalDateTime currentTime = LocalDateTime.now();
        BookingDto bookingDto = bookingDto(1, currentTime);
        Booking booking = booking(currentTime);
        booking.setBooker(user(3L));

        when(bookingRepository.findById(booking(currentTime).getId())).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class, () -> bookingService.updateBooking(bookingDto));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void updateBooking_throwsException_whenBookingIsNotWaitingStatus() {
        LocalDateTime currentTime = LocalDateTime.now();
        BookingDto bookingDto = bookingDto(2, currentTime);
        Booking booking = booking(currentTime);
        booking.setBooker(user(3L));
        booking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(booking(currentTime).getId())).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class, () -> bookingService.updateBooking(bookingDto));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void getAllBookingByUserId_whenOwnerIsExist() {
        List<Booking> bookings = new ArrayList<>();
        for (int i = 1; i < 4; i++) {
            Booking booking = Booking.builder()
                    .id((long) i)
                    .item(item())
                    .booker(user(1))
                    .status(BookingStatus.WAITING)
                    .build();
            bookings.add(booking);
        }
        Page<Booking> page = new PageImpl<>(bookings);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user(1)));
        when(bookingRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        bookingService.getAllBookingByUserId(1, BookingState.ALL.toString(), pageable);

        verify(bookingRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAllBookingByOwnerId_whenOwnerIsExist() {
        List<Booking> bookings = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Booking booking = Booking.builder()
                    .id((long) i)
                    .item(item())
                    .booker(user(1))
                    .status(BookingStatus.WAITING)
                    .build();
            bookings.add(booking);
        }
        Page<Booking> page = new PageImpl<>(bookings);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user(1)));
        when(bookingRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        bookingService.getAllBookingByOwnerId(1, BookingState.ALL.toString(), pageable);

        verify(bookingRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAllBookingByOwnerId_whenOwnerIsNotExist() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getAllBookingByOwnerId(1L, "ALL",
                pageable));
        verify(bookingRepository, never()).findAll(any(Specification.class));
    }

    @Test
    void getAllBookingByUserId_whenOwnerIsNotExist() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getAllBookingByUserId(1L, "ALL",
                pageable));
        verify(bookingRepository, never()).findAll(any(Specification.class));
    }

    @Test
    void getAllBookingByOwnerId_whenStateIncorrect() {
        assertThrows(ValidationException.class, () -> bookingService.getAllBookingByOwnerId(1L, "ILL",
                pageable));
        verify(bookingRepository, never()).findAll(any(Specification.class));
    }

    @Test
    void getAllBookingByUserId_whenStateIncorrect() {
        assertThrows(ValidationException.class, () -> bookingService.getAllBookingByUserId(1L, "ELL",
                pageable));
        verify(bookingRepository, never()).findAll(any(Specification.class));
    }

    private BookingDto bookingDto(long bookerId, LocalDateTime time) {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("item")
                .description("description")
                .available(true)
                .build();

        UserDto userDto = UserDto.builder()
                .id(1L)
                .build();

        return BookingDto.builder()
                .id(1L)
                .start(time.plusMinutes(1))
                .end(time.plusDays(2))
                .itemId(1L)
                .booker(userDto)
                .item(itemDto)
                .approved(true)
                .bookerId(bookerId)
                .status(BookingStatus.WAITING)
                .build();
    }

    private User user(long id) {
        return User.builder()
                .id(id)
                .build();
    }

    private Item item() {
        return Item.builder()
                .id(1L)
                .name("item")
                .description("description")
                .available(true)
                .owner(user(2L))
                .build();
    }

    private Booking booking(LocalDateTime time) {
        return Booking.builder()
                .id(1L)
                .start(time.plusMinutes(1))
                .end(time.plusDays(2))
                .item(item())
                .booker(user(1L))
                .status(BookingStatus.WAITING)
                .build();
    }
}