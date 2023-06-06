package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.dto.UserMapper;
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

    @Spy
    private ItemMapper itemMapper;

    @Spy
    private UserMapper userMapper;

    @Spy
    private BookingMapper bookingMapper =  new BookingMapper(itemMapper, userMapper);;

    @Spy
    private CommentMapper commentMapper;

    @InjectMocks
    private ItemServiceImpl itemService;


    @Test
    void createItem_whenItemFound() {
        User user = User.builder()
                .id(1L)
                .name("User")
                .email("user@mail.com")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("Item")
                .owner(user)
                .description("Description")
                .build();

        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Item")
                .ownerId(1L)
                .description("Description")
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.save(item)).thenReturn(item);

        ItemDto result = itemService.createItem(itemDto);

        assertNotNull(result);
        assertEquals(itemDto.getId(), result.getId());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void createItem_whenItemNotFound() {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Item")
                .ownerId(1L)
                .description("Description")
                .build();

        when(userRepository.findById(anyLong())).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> itemService.createItem(itemDto));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void updateItem_whenItemExist_andUserIsOwner() {
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
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.updateItem(itemDto);

        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals("updated item", result.getName());
        assertEquals("updated description", result.getDescription());
        assertTrue(result.getAvailable());

        verify(itemRepository, times(1)).findById(itemId);
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void updateItem_whenItemNotExist_andUserIsOwner() {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Item")
                .ownerId(1L)
                .description("Description")
                .build();

        when(itemRepository.findById(anyLong())).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> itemService.updateItem(itemDto));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void updateItem_whenItemExist_andUserNotOwner() {
        long itemId = 1L;
        long ownerId = 1L;

        ItemDto itemDto = ItemDto.builder()
                .id(itemId)
                .name("Item")
                .available(true)
                .ownerId(2L)
                .description("Description")
                .build();

        Item item = Item.builder()
                .id(itemId)
                .owner(User.builder().id(ownerId).build())
                .name("old item")
                .description("old description")
                .available(false)
                .build();

        when(itemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(item));

        assertThrows(NotFoundException.class, () -> itemService.updateItem(itemDto));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void updateItem_whenItemExist_andUserIsOwner_andItemWithoutName() {
        long itemId = 1L;
        long ownerId = 1L;

        Item item = Item.builder()
                .id(itemId)
                .owner(User.builder().id(ownerId).build())
                .name("old item")
                .description("old description")
                .available(true)
                .build();

        ItemDto itemDto = ItemDto.builder()
                .id(itemId)
                .available(true)
                .ownerId(ownerId)
                .description("new description")
                .build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).then(AdditionalAnswers.returnsFirstArg());

        ItemDto result = itemService.updateItem(itemDto);

        assertNotNull(result);
        assertEquals(item.getName(), result.getName());
        assertEquals(itemDto.getDescription(), result.getDescription());

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

        User user1 = User.builder()
                .id(2L)
                .build();

        User user2 = User.builder()
                .id(3L)
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
                .start(LocalDateTime.now().minusMonths(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(user1)
                .status(BookingStatus.WAITING)
                .build();

        Booking nextBooking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now().plusMinutes(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(user2)
                .status(BookingStatus.WAITING)
                .build();

        ItemResponseDto itemResponseDto = ItemResponseDto.builder()
                .id(itemId)
                .name("test item")
                .description("test description")
                .build();


        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(anyLong())).thenReturn(Collections.emptyList());
        when(bookingRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(lastBooking)))
                .thenReturn(new PageImpl<>(List.of(nextBooking)));

        ItemResponseDto result = itemService.getItemById(itemId, userId);

        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals("test item", result.getName());
        assertEquals("test description", result.getDescription());

        verify(userRepository, times(1)).findById(userId);
        verify(itemRepository, times(1)).findById(itemId);
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
                .start(LocalDateTime.now().minusMonths(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        List<Item> items = Collections.singletonList(item);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwnerId(anyLong(), any(Pageable.class))).thenReturn(items);
        when(bookingRepository.findAll(any(Specification.class), eq(PageRequest.of(0, 1))))
                .thenReturn(new PageImpl<>(Collections.singletonList(booking)));
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
                .id(1L)
                .text(commentDto.getText())
                .author(user)
                .item(item)
                .created(LocalDateTime.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(bookingRepository.exists(any(Specification.class))).thenReturn(true);

        CommentDto result = itemService.createComment(itemId, userId, commentDto);

        assertNotNull(result);
        assertEquals(commentDto.getText(), result.getText());
        assertEquals(user.getName(), result.getAuthorName());
    }
}