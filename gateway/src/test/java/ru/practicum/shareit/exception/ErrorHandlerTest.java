package ru.practicum.shareit.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.ConstraintViolationException;

import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({UserController.class, ItemController.class})
class ErrorHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserClient userClient;

    @MockBean
    private ItemClient itemClient;

    private static final String USER_API_PREFIX = "/users";
    private static final String ITEM_API_PREFIX = "/items";

    @SneakyThrows
    @Test
    void handleConstraintViolationException_shouldBadRequest_whenConstraintViolationException() {

        mockMvc.perform(get(USER_API_PREFIX + "/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasKey("error")))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException));

    }

    @SneakyThrows
    @Test
    void handleMissingRequestHeaderException_shouldBadRequest_whenMissingRequestHeaderException() {
        mockMvc.perform(get(ITEM_API_PREFIX + "/1")
                        .header("Not-Needed-Header", "any"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));
    }

    @SneakyThrows
    @Test
    void handleMethodArgumentNotValidException_shouldBadRequest_whenMethodArgumentNotValidException() {
        UserDto dtoWithNotValidFields = UserDto.builder()
                .name("name")
                .email("notValidMail").build();
        mockMvc.perform(post(USER_API_PREFIX)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dtoWithNotValidFields)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(result -> assertTrue(result.getResolvedException().getMessage().contains("email")));
    }

    @SneakyThrows
    @Test
    void handleThrowable_shouldInternalServerError_whenThrowable() {
        when(userClient.getAllUsers()).thenThrow(new RuntimeException());

        mockMvc.perform(get(USER_API_PREFIX))
                .andExpect(status().isInternalServerError());
    }
}