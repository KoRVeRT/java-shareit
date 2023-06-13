package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private MockMvc mockMvc;

    @Mock
    private BookingClient bookingClient;

    @InjectMocks
    private BookingController bookingController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookingController).build();
    }

    @ParameterizedTest
    @MethodSource("provideInvalidBooking")
    @SneakyThrows
    void createBooking_shouldRespondWithBadRequest_ifBookingIsInvalid(BookingDto invalidBooking) {
        String json = objectMapper.writeValueAsString(invalidBooking);
        long userId = 1L;

        mockMvc.perform(post("/bookings")
                        .header(BookingController.USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @SneakyThrows
    @Test
    void getBookingsByOwner_shouldReturnClientError_whenInvokedWithWrongState() {
        String badState = "BAD_STATE";

        Exception exception = assertThrows(NestedServletException.class, () ->
                mockMvc.perform(get("/bookings/owner")
                        .header(BookingController.USER_ID_HEADER, 1L)
                        .queryParam("state", badState))
        );

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals("Unknown state: " + badState, exception.getCause().getMessage());
    }


    private static Stream<Arguments> provideInvalidBooking() {
        return Stream.of(
                Arguments.of(bookingDto(b -> b.setItemId(null))),
                Arguments.of(bookingDto(b -> b.setStart(null))),
                Arguments.of(bookingDto(b -> b.setEnd(null))),
                Arguments.of(bookingDto(b -> b.setEnd(LocalDateTime.now().minusDays(2)))),
                Arguments.of(bookingDto(b -> b.setEnd(LocalDateTime.now()))),
                Arguments.of(bookingDto(b -> b.setStart(LocalDateTime.now().minusDays(2))))
        );
    }

    private static BookingDto bookingDto() {
        return BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusNanos(2_000_000_000))
                .end(LocalDateTime.now().plusDays(1))
                .build();
    }

    private static BookingDto bookingDto(Consumer<BookingDto> consumer) {
        BookingDto bookingDto = bookingDto();
        consumer.accept(bookingDto);
        return bookingDto;
    }
}