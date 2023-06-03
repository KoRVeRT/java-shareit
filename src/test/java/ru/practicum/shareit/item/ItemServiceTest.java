package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
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
class ItemServiceTest {
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private ItemServiceImpl itemService;


    @Test
    void createItemTest() {
        User user = new User();
        user.setId(1L);

        Item item = new Item();
        item.setId(1L);
        item.setOwner(user);

        ItemDto itemDto = new ItemDto();
        itemDto.setOwnerId(user.getId());

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemMapper.toItem(any(ItemDto.class))).thenReturn(item);
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.toItemDto(any(Item.class))).thenReturn(itemDto);

        ItemDto result = itemService.createItem(itemDto);

        assertNotNull(result);
        assertEquals(itemDto.getOwnerId(), result.getOwnerId());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void updateItemTest() {
        long itemId = 1L;
        long ownerId = 1L;

        ItemDto itemDto = ItemDto.builder()
                .id(itemId)
                .ownerId(ownerId)
                .name("updated item")
                .description("updated description")
                .available(true)
                .build();

        Item item = Item.builder()
                .id(itemId)
                .owner(User.builder().id(ownerId).build())
                .name("old item")
                .description("old description")
                .available(false)
                .build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.updateItem(itemDto);

        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals(ownerId, result.getOwnerId());
        assertEquals("updated item", result.getName());
        assertEquals("updated description", result.getDescription());
        assertTrue(result.getAvailable());

        verify(itemRepository, times(1)).findById(itemId);
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void deleteItemTest() {
        long itemId = 1L;

        doNothing().when(itemRepository).deleteById(itemId);

        assertDoesNotThrow(() -> itemService.deleteItem(itemId));

        verify(itemRepository, times(1)).deleteById(itemId);
    }

    @Test
    void getItemByIdTest() {
        long itemId = 1L;
        long userId = 1L;

        User user = User.builder()
                .id(userId)
                .build();

        Item item = Item.builder()
                .id(itemId)
                .owner(user)
                .name("test item")
                .description("test description")
                .available(true)
                .build();

        Booking lastBooking = Booking.builder()
                .id(1L)
                .build();

        Booking nextBooking = Booking.builder()
                .id(2L)
                .build();

        ItemResponseDto itemResponseDto = ItemResponseDto.builder()
                .id(itemId)
                .name("test item")
                .description("test description")
                .build();


        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemMapper.toItemResponseDto(item)).thenReturn(itemResponseDto);
        when(bookingMapper.toBookingDto(lastBooking)).thenReturn(new BookingDto());
        when(bookingMapper.toBookingDto(nextBooking)).thenReturn(new BookingDto());
        when(commentRepository.findByItemId(anyLong())).thenReturn(Collections.emptyList());

        when(bookingRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(
                new PageImpl<>(List.of(lastBooking), PageRequest.of(0, 1), 1),
                new PageImpl<>(List.of(nextBooking), PageRequest.of(0, 1), 1)
        );

        ItemResponseDto result = itemService.getItemById(itemId, userId);

        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals("test item", result.getName());
        assertEquals("test description", result.getDescription());

        verify(userRepository, times(1)).findById(userId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(itemMapper, times(1)).toItemResponseDto(item);
        verify(bookingRepository, times(2)).findAll(any(Specification.class), any(Pageable.class));
    }


    @Test
    void getAllItemsByUserIdTest() {
        long userId = 1L;
        User user = User.builder()
                .id(userId)
                .build();

        Item item = Item.builder()
                .id(1L)
                .owner(user)
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        List<Item> items = Collections.singletonList(item);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwnerId(anyLong(), any(Pageable.class))).thenReturn(items);
        when(itemMapper.toItemResponseDto(item)).thenReturn(new ItemResponseDto());
        when(bookingRepository.findAll(any(Specification.class), eq(PageRequest.of(0, 1))))
                .thenReturn(new PageImpl<>(Collections.singletonList(booking)));
        when(bookingMapper.toBookingDto(booking)).thenReturn(new BookingDto());
        when(commentRepository.findByItemId(anyLong())).thenReturn(Collections.emptyList());


        List<ItemResponseDto> result = itemService.getAllItemsByUserId(userId, pageable);


        assertNotNull(result);
        assertEquals(1, result.size());
        verify(itemMapper, times(1)).toItemResponseDto(item);
        verify(bookingMapper, times(2)).toBookingDto(booking);
    }

    @Test
    void searchItemsByTextTest() {
        String searchText = "test";
        int page = 0;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);

        Item item = Item.builder()
                .id(1L)
                .name("test item")
                .description("test description")
                .available(true)
                .build();

        ItemDto itemDto = ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .ownerId(2L)
                .requestId(3L)
                .build();

        when(itemRepository.findItemsByText(searchText, pageable)).thenReturn(List.of(item));
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        List<ItemDto> result = itemService.searchItemsByText(searchText, pageable);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(item.getName(), result.get(0).getName());
        assertEquals(item.getDescription(), result.get(0).getDescription());
    }

    @Test
    void createCommentTest() {
        long itemId = 1L;
        long userId = 1L;

        CommentDto commentDto = CommentDto.builder()
                .text("test comment")
                .authorName("test user")
                .build();

        User user = User.builder()
                .id(userId)
                .name("test user")
                .build();

        Item item = Item.builder()
                .id(itemId)
                .owner(user)
                .name("test item")
                .description("test description")
                .available(true)
                .build();

        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .author(user)
                .item(item)
                .created(LocalDateTime.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(commentMapper.toComment(commentDto, item, user)).thenReturn(comment);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(bookingRepository.exists(any(Specification.class))).thenReturn(true);
        when(commentMapper.toCommentDto(comment)).thenReturn(commentDto);

        CommentDto result = itemService.createComment(itemId, userId, commentDto);

        assertNotNull(result);
        assertEquals(commentDto.getText(), result.getText());
        assertEquals(user.getName(), result.getAuthorName());
    }
}