package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ItemController.class)
class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @MockBean
    private ItemRequestRepository itemRequestRepository;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ItemDto itemDto;
    private ItemResponseDto itemResponseDto;

    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        itemDto = ItemDto.builder()
                .id(1L)
                .name("test item")
                .description("test description")
                .available(true)
                .build();

        itemResponseDto = ItemResponseDto.builder()
                .id(1L)
                .name("test item")
                .description("test description")
                .available(true)
                .build();

        commentDto = CommentDto.builder()
                .id(1L)
                .text("test comment")
                .authorName("test user")
                .created(LocalDateTime.now())
                .build();

        itemResponseDto.setComments(Collections.singletonList(commentDto));
    }

    @Test
    void createItemTest() throws Exception {
        when(itemService.createItem(any(ItemDto.class))).thenReturn(itemDto);

        this.mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(itemDto)));
    }

    @Test
    void updateItemTest() throws Exception {
        when(itemService.updateItem(any(ItemDto.class))).thenReturn(itemDto);

        this.mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(itemDto)));
    }

    @Test
    void deleteItemTest() throws Exception {
        this.mockMvc.perform(delete("/items/1"))
                .andExpect(status().isOk());

        verify(itemService, times(1)).deleteItem(1L);
    }

    @Test
    void returnItemByIdTest() throws Exception {
        when(itemService.getItemById(1L, 1L)).thenReturn(itemResponseDto);

        this.mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(itemResponseDto)));
    }

    @Test
    void createCommentTest() throws Exception {
        when(itemService.createComment(1L, 1L, commentDto)).thenReturn(commentDto);

        this.mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(commentDto)));
    }
}