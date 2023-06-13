package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private MockMvc mockMvc;

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookingController).build();
    }

    @Test
    void createBooking_shouldRespondWithOk_ifBookingIsValid() throws Exception {
        when(bookingService.createBooking(any(BookingDto.class))).thenReturn(new BookingDto());

        String json = objectMapper.writeValueAsString(bookingDto());

        mockMvc.perform(post("/bookings")
                        .header(BookingController.USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(bookingService, times(1)).createBooking(any(BookingDto.class));
    }

    @Test
    void updateBooking_shouldRespondWithOk() throws Exception {
        long bookingId = 2L;
        long userId = 2L;
        boolean approved = true;
        when(bookingService.updateBooking(any(BookingDto.class))).thenReturn(new BookingDto());

        mockMvc.perform(patch("/bookings/" + bookingId)
                        .header(BookingController.USER_ID_HEADER, userId)
                        .param("approved", String.valueOf(approved))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingService, times(1)).updateBooking(any(BookingDto.class));
    }

    @Test
    void getBookingById_shouldRespondWithOk() throws Exception {
        long bookingId = 3L;
        long userId = 3L;
        when(bookingService.getBookingById(anyLong(), anyLong())).thenReturn(new BookingDto());

        mockMvc.perform(get("/bookings/" + bookingId)
                        .header(BookingController.USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingService, times(1)).getBookingById(anyLong(), anyLong());
    }

    @Test
    void getAllBookingByUserId_shouldRespondWithOk_whenUseTwoCorrectParametersFromAndSize() throws Exception {
        long userId = 1L;
        String state = "ALL";
        long from = 10;
        long size = 20;
        when(bookingService.getAllBookingByUserId(anyLong(), anyString(), any(Pageable.class)))
                .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/bookings")
                        .header(BookingController.USER_ID_HEADER, userId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingService, times(1)).getAllBookingByUserId(anyLong(), anyString(),
                any(Pageable.class));
    }

    @Test
    void getAllBookingByUserId_shouldRespondWithOk_whenWithoutParameters() throws Exception {
        long userId = 1L;
        when(bookingService.getAllBookingByUserId(anyLong(), anyString(), any(Pageable.class)))
                .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/bookings")
                        .header(BookingController.USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingService, times(1)).getAllBookingByUserId(anyLong(), anyString(),
                any(Pageable.class));
    }

    @Test
    void getAllBookingByOwnerId_shouldRespondWithOk_whenUseTwoCorrectParametersFromAndSize() throws Exception {
        long ownerId = 1L;
        String state = "ALL";
        long from = 10;
        long size = 20;
        when(bookingService.getAllBookingByOwnerId(anyLong(), anyString(), any(Pageable.class)))
                .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/bookings/owner")
                        .header(BookingController.USER_ID_HEADER, ownerId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingService, times(1)).getAllBookingByOwnerId(anyLong(), anyString(),
                any(Pageable.class));
    }

    @Test
    void getAllBookingByOwnerId_shouldRespondWithOk_whenWithoutParameters() throws Exception {
        long ownerId = 1L;
        when(bookingService.getAllBookingByOwnerId(anyLong(), anyString(), any(Pageable.class)))
                .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/bookings/owner")
                        .header(BookingController.USER_ID_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingService, times(1)).getAllBookingByOwnerId(anyLong(), anyString(),
                any(Pageable.class));
    }

    private static BookingDto bookingDto() {
        return BookingDto.builder()
                .bookerId(1L)
                .itemId(1L)
                .start(LocalDateTime.now().plusNanos(2_000_000_000))
                .end(LocalDateTime.now().plusDays(1))
                .build();
    }
}