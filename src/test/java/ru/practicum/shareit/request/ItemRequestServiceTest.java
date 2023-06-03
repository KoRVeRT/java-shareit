package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemRequestMapper itemRequestMapper;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    void createRequestTest() {
        User user = new User();
        user.setId(1L);

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setRequestor(user);

        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setRequestorId(user.getId());

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestMapper.toItemRequest(any(ItemRequestDto.class))).thenReturn(itemRequest);
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);
        when(itemRequestMapper.toItemRequestDto(any(ItemRequest.class))).thenReturn(itemRequestDto);

        ItemRequestDto result = itemRequestService.createRequest(itemRequestDto);

        assertNotNull(result);
        assertEquals(itemRequestDto.getRequestorId(), result.getRequestorId());
        verify(itemRequestRepository, times(1)).save(any(ItemRequest.class));
    }

    @Test
    void getAllRequestsTest() {
        User user = new User();
        user.setId(1L);

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setRequestor(user);

        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setRequestorId(user.getId());

        List<ItemRequest> itemRequests = Collections.singletonList(itemRequest);
        Page<ItemRequest> page = new PageImpl<>(itemRequests);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(itemRequestMapper.toItemRequestDto(any(ItemRequest.class))).thenReturn(itemRequestDto);

        List<ItemRequestDto> result = itemRequestService.getAllRequests(0, 10, user.getId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(itemRequestDto.getRequestorId(), result.get(0).getRequestorId());
        verify(itemRequestRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getRequestByIdTest() {
        User user = new User();
        user.setId(1L);

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setRequestor(user);

        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setRequestorId(user.getId());

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(itemRequest));
        when(itemRequestMapper.toItemRequestDto(any(ItemRequest.class))).thenReturn(itemRequestDto);

        ItemRequestDto result = itemRequestService.getRequestById(1L, user.getId());

        assertNotNull(result);
        assertEquals(itemRequestDto.getRequestorId(), result.getRequestorId());
        verify(itemRequestRepository, times(1)).findById(anyLong());
    }

    @Test
    void getOwnRequestsByUserIdTest() {
        User user = new User();
        user.setId(1L);

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setRequestor(user);

        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setRequestorId(user.getId());

        List<ItemRequest> itemRequests = Collections.singletonList(itemRequest);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(itemRequests);
        when(itemRequestMapper.toItemRequestDto(any(ItemRequest.class))).thenReturn(itemRequestDto);


        List<ItemRequestDto> result = itemRequestService.getOwnRequestsByUserId(user.getId());


        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(itemRequestDto.getRequestorId(), result.get(0).getRequestorId());
        verify(itemRequestRepository, times(1)).findAll(any(Specification.class), any(Sort.class));
    }
}