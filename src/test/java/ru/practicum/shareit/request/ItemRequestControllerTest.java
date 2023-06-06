package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createRequest_whenRequestValid() throws Exception {
        long userId = 1L;
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("New request")
                .requestorId(userId)
                .created(LocalDateTime.now())
                .build();
        ItemRequestDto createdRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("New request")
                .requestorId(userId)
                .created(LocalDateTime.now())
                .build();

        when(itemRequestService.createRequest(requestDto)).thenReturn(createdRequestDto);

        mockMvc.perform(post("/requests")
                        .header(ItemRequestController.USER_ID_HEADER, String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdRequestDto.getId()))
                .andExpect(jsonPath("$.description").value(createdRequestDto.getDescription()))
                .andExpect(jsonPath("$.requestorId").value(createdRequestDto.getRequestorId()))
                .andExpect(jsonPath("$.created").exists());

        verify(itemRequestService, times(1)).createRequest(requestDto);
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    void createRequest_throwException_whenRequestNotValidDescription_andReturnStatus() throws Exception {
        long userId = 1L;
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description(" ")
                .requestorId(userId)
                .created(LocalDateTime.now())
                .build();
        ItemRequestDto createdRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("New request")
                .requestorId(userId)
                .created(LocalDateTime.now())
                .build();

        when(itemRequestService.createRequest(requestDto)).thenReturn(createdRequestDto);

        mockMvc.perform(post("/requests")
                        .header(ItemRequestController.USER_ID_HEADER, String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

    }

    @Test
    void getAllRequests_whenRequestParametersValid() throws Exception {
        long userId = 1L;
        int from = 0;
        int size = 10;
        List<ItemRequestDto> requests = Arrays.asList(
                ItemRequestDto.builder()
                        .id(1L)
                        .description("Request 1")
                        .requestorId(userId)
                        .created(LocalDateTime.now())
                        .build(),
                ItemRequestDto.builder()
                        .id(2L)
                        .description("Request 2")
                        .requestorId(userId)
                        .created(LocalDateTime.now())
                        .build()
        );

        when(itemRequestService.getAllRequests(from, size, userId)).thenReturn(requests);

        mockMvc.perform(get("/requests/all")
                        .header(ItemRequestController.USER_ID_HEADER, String.valueOf(userId))
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(requests.size()))
                .andExpect(jsonPath("$[0].id").value(requests.get(0).getId()))
                .andExpect(jsonPath("$[0].description").value(requests.get(0).getDescription()))
                .andExpect(jsonPath("$[0].requestorId").value(requests.get(0).getRequestorId()))
                .andExpect(jsonPath("$[0].created").exists())
                .andExpect(jsonPath("$[1].id").value(requests.get(1).getId()))
                .andExpect(jsonPath("$[1].description").value(requests.get(1).getDescription()))
                .andExpect(jsonPath("$[1].requestorId").value(requests.get(1).getRequestorId()))
                .andExpect(jsonPath("$[1].created").exists());

        verify(itemRequestService, times(1)).getAllRequests(from, size, userId);
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    void getAllRequests_throwException_whenRequestParameterFromNotValidFrom_returnStatus() throws Exception {
        long userId = 1L;
        int from = -1;
        int size = 10;
        List<ItemRequestDto> requests = Arrays.asList(
                ItemRequestDto.builder()
                        .id(1L)
                        .description("Request 1")
                        .requestorId(userId)
                        .created(LocalDateTime.now())
                        .build(),
                ItemRequestDto.builder()
                        .id(2L)
                        .description("Request 2")
                        .requestorId(userId)
                        .created(LocalDateTime.now())
                        .build()
        );

        when(itemRequestService.getAllRequests(from, size, userId)).thenReturn(requests);

        mockMvc.perform(get("/requests/all")
                        .header(ItemRequestController.USER_ID_HEADER, String.valueOf(userId))
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllRequests_throwException_whenRequestParameterFromNotValidSize_returnStatus() throws Exception {
        long userId = 1L;
        int from = 0;
        int size = -10;
        List<ItemRequestDto> requests = Arrays.asList(
                ItemRequestDto.builder()
                        .id(1L)
                        .description("Request 1")
                        .requestorId(userId)
                        .created(LocalDateTime.now())
                        .build(),
                ItemRequestDto.builder()
                        .id(2L)
                        .description("Request 2")
                        .requestorId(userId)
                        .created(LocalDateTime.now())
                        .build()
        );

        when(itemRequestService.getAllRequests(from, size, userId)).thenReturn(requests);

        mockMvc.perform(get("/requests/all")
                        .header(ItemRequestController.USER_ID_HEADER, String.valueOf(userId))
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOwnRequestsByUserId_whenUserFound() throws Exception {
        long userId = 1L;
        List<ItemRequestDto> requests = Arrays.asList(
                ItemRequestDto.builder()
                        .id(1L)
                        .description("Request 1")
                        .requestorId(userId)
                        .created(LocalDateTime.now())
                        .items(List.of(ItemDto.builder()
                                .id(1L)
                                .name("Item")
                                .description("description")
                                .ownerId(1L)
                                .build())
                        )
                        .build(),
                ItemRequestDto.builder()
                        .id(2L)
                        .description("Request 2")
                        .requestorId(userId)
                        .created(LocalDateTime.now())
                        .build()
        );

        when(itemRequestService.getOwnRequestsByUserId(userId)).thenReturn(requests);

        mockMvc.perform(get("/requests")
                        .header(ItemRequestController.USER_ID_HEADER, String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(requests.size()))
                .andExpect(jsonPath("$[0].id").value(requests.get(0).getId()))
                .andExpect(jsonPath("$[0].description").value(requests.get(0).getDescription()))
                .andExpect(jsonPath("$[0].requestorId").value(requests.get(0).getRequestorId()))
                .andExpect(jsonPath("$[0].created").exists())
                .andExpect(jsonPath("$[0].items").exists())
                .andExpect(jsonPath("$[1].id").value(requests.get(1).getId()))
                .andExpect(jsonPath("$[1].description").value(requests.get(1).getDescription()))
                .andExpect(jsonPath("$[1].requestorId").value(requests.get(1).getRequestorId()))
                .andExpect(jsonPath("$[1].created").exists());

        verify(itemRequestService, times(1)).getOwnRequestsByUserId(userId);
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    void getOwnRequestsByUserId_whenUserNotFound_returnStatus() throws Exception {
        long userId = 1L;

        when(itemRequestService.getOwnRequestsByUserId(userId)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/requests")
                        .header(ItemRequestController.USER_ID_HEADER, String.valueOf(userId)))
                .andExpect(status().isNotFound());


        verify(itemRequestService, times(1)).getOwnRequestsByUserId(userId);
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    void getRequestById_whenRequestFound() throws Exception {
        long userId = 1L;
        long requestId = 1L;
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("Request")
                .requestorId(userId)
                .items(List.of(ItemDto.builder()
                        .id(1L)
                        .name("Item")
                        .description("description")
                        .ownerId(1L)
                        .build())
                )
                .created(LocalDateTime.now())
                .build();

        when(itemRequestService.getRequestById(requestId, userId)).thenReturn(requestDto);

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(ItemRequestController.USER_ID_HEADER, String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestDto.getId()))
                .andExpect(jsonPath("$.description").value(requestDto.getDescription()))
                .andExpect(jsonPath("$.requestorId").value(requestDto.getRequestorId()))
                .andExpect(jsonPath("$.items.size()").value(requestDto.getItems().size()))
                .andExpect(jsonPath("$.items").exists())
                .andExpect(jsonPath("$.created").exists());

        verify(itemRequestService, times(1)).getRequestById(requestId, userId);
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    void getRequestById_whenRequestNotFound_returnStatus() throws Exception {
        long userId = 1L;
        long requestId = 1L;

        when(itemRequestService.getRequestById(requestId, userId)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(ItemRequestController.USER_ID_HEADER, String.valueOf(userId)))
                .andExpect(status().isNotFound());


        verify(itemRequestService, times(1)).getRequestById(requestId, userId);
        verifyNoMoreInteractions(itemRequestService);
    }
}