package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingDto bookingDto;
    private BookingResponseDto bookingResponseDto1;
    private BookingResponseDto bookingResponseDto2;

    @BeforeEach
    public void setUp() {
        bookingDto = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusMinutes(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
                .bookerId(1L)
                .build();

        bookingResponseDto1 = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusMinutes(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(new ItemDto())
                .booker(new UserDto())
                .status(BookingStatus.APPROVED)
                .build();

        bookingResponseDto2 = BookingResponseDto.builder()
                .id(2L)
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .status(BookingStatus.APPROVED)
                .build();
    }

    @Test
    void createBookingTest() throws Exception {
        when(bookingService.createBooking(any(BookingDto.class))).thenReturn(bookingResponseDto1);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingResponseDto1.getId()))
                .andExpect(jsonPath("$.status").value(bookingResponseDto1.getStatus().toString()));
    }

    @Test
    void updateBookingTest() throws Exception {
        when(bookingService.updateBooking(any(BookingDto.class))).thenReturn(bookingResponseDto1);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingResponseDto1.getId()))
                .andExpect(jsonPath("$.status").value(bookingResponseDto1.getStatus().toString()));
    }

    @Test
    void getBookingByIdTest() throws Exception {
        when(bookingService.getBookingById(anyLong(), anyLong())).thenReturn(bookingResponseDto1);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingResponseDto1.getId()))
                .andExpect(jsonPath("$.status").value(bookingResponseDto1.getStatus().toString()));
    }

    @Test
    void getAllBookingByUserIdTest() throws Exception {
        List<BookingResponseDto> bookingsResponseDto = List.of(bookingResponseDto1, bookingResponseDto2);

        when(bookingService.getAllBookingByUserId(anyLong(), any(BookingState.class), any(Pageable.class)))
                .thenReturn(bookingsResponseDto);

        mockMvc.perform(get("/bookings")
                        .header(BookingController.USER_ID_HEADER, "1")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(bookingsResponseDto)));
    }

    @Test
    void getAllBookingByOwnerIdTest() throws Exception {
        List<BookingResponseDto> bookingsResponseDto = List.of(bookingResponseDto1, bookingResponseDto2);

        when(bookingService.getAllBookingByOwnerId(anyLong(), any(BookingState.class), any(Pageable.class)))
                .thenReturn(bookingsResponseDto);

        mockMvc.perform(get("/bookings/owner")
                        .header(BookingController.USER_ID_HEADER, "1")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(bookingsResponseDto)));
    }
}

