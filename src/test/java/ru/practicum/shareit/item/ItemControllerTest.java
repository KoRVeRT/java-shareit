package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ItemController.class)
class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createItemTest_whenItemCreatedReturnStatusOk() throws Exception {
        String json = objectMapper.writeValueAsString(createItemDto());

        when(itemService.createItem(any(ItemDto.class))).thenReturn(createItemDto());

        this.mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void createItemTest_whenItemNameIncorrectReturnBadRequest() throws Exception {
        ItemDto itemDto = createItemDto();
        itemDto.setName("");
        String json = objectMapper.writeValueAsString(itemDto);

        when(itemService.createItem(any(ItemDto.class))).thenReturn(itemDto);

        this.mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItemTest_whenItemDescriptionIncorrectReturnBadRequest() throws Exception {
        ItemDto itemDto = createItemDto();
        itemDto.setDescription("");
        String json = objectMapper.writeValueAsString(itemDto);

        when(itemService.createItem(any(ItemDto.class))).thenReturn(itemDto);

        this.mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItemTest_whenItemAvailableIncorrectReturnBadRequest() throws Exception {
        ItemDto itemDto = createItemDto();
        itemDto.setAvailable(null);
        String json = objectMapper.writeValueAsString(itemDto);

        when(itemService.createItem(any(ItemDto.class))).thenReturn(itemDto);

        this.mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_whenItemUpdatedReturnStatusOk() throws Exception {
        String json = objectMapper.writeValueAsString(createItemDto());

        when(itemService.updateItem(any(ItemDto.class))).thenReturn(createItemDto());

        this.mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void deleteItem_whenItemUpdatedReturnStatusOk() throws Exception {
        this.mockMvc.perform(delete("/items/1"))
                .andExpect(status().isOk());

        verify(itemService, times(1)).deleteItem(1L);
    }

    @Test
    void returnItemById_andStatusOk() throws Exception {
        when(itemService.getItemById(1L, 1L)).thenReturn(createItemDto());

        this.mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void createCommentTest_returnStatusOk() throws Exception {
        String json = objectMapper.writeValueAsString(createCommentDto());

        when(itemService.createComment(1L, 1L, createCommentDto())).thenReturn(createCommentDto());

        this.mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void createCommentTest_returnStatusBadRequest() throws Exception {
        CommentDto commentDto = createCommentDto();
        commentDto.setText(" ");
        String json = objectMapper.writeValueAsString(commentDto);

        when(itemService.createComment(1L, 1L, createCommentDto())).thenReturn(createCommentDto());

        this.mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    private ItemDto createItemDto() {
        CommentDto commentDto = createCommentDto();
        return ItemDto.builder()
                .id(1L)
                .name("test item")
                .description("test description")
                .available(true)
                .comments(Collections.singletonList(commentDto))
                .build();
    }

    private CommentDto createCommentDto() {
        return CommentDto.builder()
                .id(1L)
                .text("test comment")
                .authorName("test user")
                .created(LocalDateTime.now())
                .build();
    }
}