package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    private static final String API_PREFIX = "/items";

    @Test
    @SneakyThrows
    void getAllItems_shouldReturnStatusBadRequest_whenParamSizeIs0() {
        mockMvc.perform(get(API_PREFIX)
                        .header(ItemController.USER_ID_HEADER, 1)
                        .queryParam("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("size must be positive")));

        verifyNoInteractions(itemClient);
    }

    @Test
    @SneakyThrows
    void getAllItems_shouldReturnStatusBadRequest_whenParamFromIsNegative() {
        mockMvc.perform(get(API_PREFIX)
                        .header(ItemController.USER_ID_HEADER, 1)
                        .queryParam("from", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("from cannot be negative")));

        verifyNoInteractions(itemClient);
    }

    @Test
    @SneakyThrows
    void findByText_shouldReturnStatusBadRequest_whenParamSizeIs0() {
        mockMvc.perform(get(API_PREFIX + "/search")
                        .header(ItemController.USER_ID_HEADER, 1)
                        .param("text", "item")
                        .queryParam("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("size must be positive")));

        verifyNoInteractions(itemClient);
    }

    @Test
    @SneakyThrows
    void findByText_shouldReturnStatusBadRequest_whenParamFromIsNegative() {
        mockMvc.perform(get(API_PREFIX + "/search")
                        .header(ItemController.USER_ID_HEADER, 1)
                        .param("text", "item")
                        .queryParam("from", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("from cannot be negative")));

        verifyNoInteractions(itemClient);
    }

    @Test
    @SneakyThrows
    void createComment_shouldReturnStatusBadRequest_ifCommentIsInvalid() {
        CommentDto commentDto = CommentDto.builder()
                .text(" ")
                .build();
        String json = objectMapper.writeValueAsString(commentDto);

        mockMvc.perform(post(API_PREFIX + "/1/comment")
                        .header(ItemController.USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }


    @ParameterizedTest
    @MethodSource("provideInvalidItem")
    @SneakyThrows
    void createItem_shouldRespondWithBadRequest_ifItemIsInvalid(ItemDto invalidItemDto) {
        String json = objectMapper.writeValueAsString(invalidItemDto);

        mockMvc.perform(post(API_PREFIX)
                        .header(ItemController.USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verifyNoInteractions(itemClient);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidUpdateItem")
    @SneakyThrows
    void updateItem_shouldRespondWithBadRequest_ifItemIsInvalid(ItemDto invalidUpdateItemDto) {
        String json = objectMapper.writeValueAsString(invalidUpdateItemDto);

        mockMvc.perform(patch(API_PREFIX + "/1")
                        .header(ItemController.USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verifyNoInteractions(itemClient);
    }

    private static Stream<Arguments> provideInvalidItem() {
        return Stream.of(
                Arguments.of(itemDto(b -> b.setName(null))),
                Arguments.of(itemDto(b -> b.setAvailable(null))),
                Arguments.of(itemDto(b -> b.setDescription(null))),
                Arguments.of(itemDto(b -> b.setName(" "))),
                Arguments.of(itemDto(b -> b.setDescription(" ")))
        );
    }

    private static Stream<Arguments> provideInvalidUpdateItem() {
        return Stream.of(
                Arguments.of(itemDto(b -> b.setName(" "))),
                Arguments.of(itemDto(b -> b.setDescription(" ")))

        );
    }

    private static ItemDto itemDto() {
        return ItemDto.builder()
                .name("name")
                .available(true)
                .description("description")
                .build();
    }

    private static ItemDto itemDto(Consumer<ItemDto> consumer) {
        ItemDto itemDto = itemDto();
        consumer.accept(itemDto);
        return itemDto;
    }
}