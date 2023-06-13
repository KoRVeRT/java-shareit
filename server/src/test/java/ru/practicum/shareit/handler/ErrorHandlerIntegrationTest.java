package ru.practicum.shareit.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.ValidationException;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({UserController.class, ItemController.class})
class ErrorHandlerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @SneakyThrows
    void handleNotFoundExceptionStatusTest() {
        long userId = 1L;

        when(userService.getUserById(userId)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(userId);
        verifyNoMoreInteractions(userService);
    }

    @Test
    @SneakyThrows
    void handleValidationExceptionStatusTest() {
        long itemId = 1L;
        CommentDto commentDto = CommentDto.builder()
                .text("test comment")
                .authorName("test user")
                .created(LocalDateTime.now())
                .build();

        when(itemService.createComment(anyLong(), anyLong(), any(CommentDto.class))).thenThrow(ValidationException.class);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void handleExceptionTest() {
        long userId = 1L;

        when(userService.getUserById(userId)).thenThrow(RuntimeException.class);

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isInternalServerError());

        verify(userService, times(1)).getUserById(userId);
        verifyNoMoreInteractions(userService);
    }
}