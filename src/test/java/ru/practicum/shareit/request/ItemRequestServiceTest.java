package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
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

    @Spy
    private ItemMapper itemMapper;

    @Spy
    private ItemRequestMapper itemRequestMapper = new ItemRequestMapper(new UserMapper());

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;


    @Test
    void createRequest_whenUserFound() {
        User user = new User();
        user.setId(1L);

        UserDto userDto = new UserDto();
        userDto.setId(1L);

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(1L);

        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(1L);
        itemRequestDto.setRequestorId(userDto.getId());

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestRepository.save(any(ItemRequest.class))).then(AdditionalAnswers.returnsFirstArg());


        ItemRequestDto result = itemRequestService.createRequest(itemRequestDto);

        assertNotNull(result);
        assertNotNull(result.getCreated());
        assertNotNull(result.getRequestor());
        assertEquals(itemRequestDto.getRequestorId(), result.getRequestor().getId());
        verify(itemRequestRepository, times(1)).save(any(ItemRequest.class));
    }

    @Test
    void createRequest_whenUserNotFound() {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(1L);
        itemRequestDto.setRequestorId(1L);

        when(userRepository.findById(anyLong())).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> itemRequestService.createRequest(itemRequestDto));

        verify(itemRequestRepository, never()).save(any(ItemRequest.class));
    }

    @Test
    void getAllRequests_whenUserFound() {
        User user = new User();
        user.setId(1L);

        ItemRequest itemRequest1 = new ItemRequest();
        itemRequest1.setId(1L);
        itemRequest1.setCreated(LocalDateTime.now());
        itemRequest1.setRequestor(user);

        ItemRequest itemRequest2 = new ItemRequest();
        itemRequest2.setId(2L);
        itemRequest2.setCreated(LocalDateTime.now());
        itemRequest2.setRequestor(user);

        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Item 1");

        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Item 2");

        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setRequestorId(user.getId());

        List<ItemRequest> itemRequests = Arrays.asList(itemRequest1, itemRequest2);
        Page<ItemRequest> page = new PageImpl<>(itemRequests);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findAllByRequestId(anyLong())).thenReturn(Arrays.asList(item1, item2));
        when(itemRequestRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        List<ItemRequestDto> result = itemRequestService.getAllRequests(0, 10, user.getId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertEquals(itemRequestDto.getRequestorId(), result.get(0).getRequestor().getId());
        assertEquals(2, result.get(0).getItems().size());
        verify(itemRequestRepository, times(1)).findAll(any(Specification.class),
                any(Pageable.class));
    }

    @Test
    void getAllRequests_whenUserNotFound() {
        when(userRepository.findById(anyLong())).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> itemRequestService.getAllRequests(0, 10, 1L));
    }

    @Test
    void getRequestById_whenUserFound_andWithoutItems() {
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

        ItemRequestDto result = itemRequestService.getRequestById(1L, user.getId());

        assertNotNull(result);
        assertEquals(itemRequestDto.getRequestorId(), result.getRequestor().getId());
        assertTrue(result.getItems().isEmpty());
        verify(itemRequestRepository, times(1)).findById(anyLong());
    }

    @Test
    void getRequestById_whenUserNotFound() {
        when(userRepository.findById(anyLong())).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(1L, 1L));
    }

    @Test
    void getOwnRequestsByUserId_whenUserFound_andWithItems() {
        User user = new User();
        user.setId(1L);

        ItemRequest itemRequest1 = new ItemRequest();
        itemRequest1.setId(1L);
        itemRequest1.setCreated(LocalDateTime.now());
        itemRequest1.setRequestor(user);

        ItemRequest itemRequest2 = new ItemRequest();
        itemRequest2.setId(2L);
        itemRequest2.setCreated(LocalDateTime.now());
        itemRequest2.setRequestor(user);

        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Item 1");

        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Item 2");

        Item item3 = new Item();
        item3.setId(2L);
        item3.setName("Item 2");

        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setRequestorId(user.getId());

        List<ItemRequest> itemRequests = Arrays.asList(itemRequest1, itemRequest2);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findAllByRequestId(anyLong())).thenReturn(Arrays.asList(item1, item2, item3))
                .thenReturn((Arrays.asList(item1, item2)));
        when(itemRequestRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(itemRequests);

        List<ItemRequestDto> result = itemRequestService.getOwnRequestsByUserId(user.getId());


        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertEquals(itemRequestDto.getRequestorId(), result.get(0).getRequestor().getId());
        assertEquals(2, result.get(1).getItems().size());
        assertEquals(3, result.get(0).getItems().size());
        verify(itemRequestRepository, times(1)).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    void getOwnRequestsByUserId_whenUserNotFound() {
        when(userRepository.findById(anyLong())).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> itemRequestService.getOwnRequestsByUserId(1L));
    }
}