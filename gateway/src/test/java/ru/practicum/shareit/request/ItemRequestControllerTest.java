package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ItemRequestController.class)
class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestClient requestClient;

    @SneakyThrows
    @Test
    void getRequests_shouldThrowException_whenSizeParamIsNegative() {

        String responseBody = mockMvc.perform(get("/requests/all")
                        .header(ItemRequestController.USER_ID_HEADER, 1)
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(responseBody.contains("size must be positive"));
        verifyNoInteractions(requestClient);
    }

    @SneakyThrows
    @Test
    void getRequests_shouldThrowException_whenFromParamIsNegative() {

        String responseBody = mockMvc.perform(get("/requests/all")
                        .header(ItemRequestController.USER_ID_HEADER, 1)
                        .param("from", "-1"))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(responseBody.contains("from cannot be negative"));
        verifyNoInteractions(requestClient);
    }

    @SneakyThrows
    @Test
    void createRequest_shouldThrowException_whenRequestIsInvalid() {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("")
                .build();
        String json = objectMapper.writeValueAsString(itemRequestDto);
        mockMvc.perform(post("/requests")
                        .header(ItemRequestController.USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verifyNoInteractions(requestClient);
    }
}