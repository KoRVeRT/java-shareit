package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.repository.BookingSpecifications;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingSpecificationsTest {

    @Mock
    BookingSpecifications bookingSpecifications;

    @Mock
    BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void existsBookerIdAndItemIdAndEndBefore() {
        long itemId = 1L;
        long userId = 1L;
        CommentDto commentDto = new CommentDto();
        User user = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(new Item()));
        when(bookingRepository.exists(any(Specification.class))).thenReturn(true);
        when(commentMapper.toComment(any(CommentDto.class), any(Item.class), any(User.class))).thenReturn(new Comment());
        when(commentRepository.save(any(Comment.class))).thenReturn(new Comment());

        itemService.createComment(itemId, userId, commentDto);

        verify(bookingRepository).exists(any(Specification.class));
    }


    @Test
    void findFirstByItemIdAndStartBeforeOrderByStartDesc() {
        long itemId = 1L;
        long userId = 1L;
        CommentDto commentDto = new CommentDto();
        User user = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(new Item()));
        when(bookingRepository.exists(any(Specification.class))).thenReturn(true);
        when(commentMapper.toComment(any(CommentDto.class), any(Item.class), any(User.class))).thenReturn(new Comment());
        when(commentRepository.save(any(Comment.class))).thenReturn(new Comment());

        itemService.createComment(itemId, userId, commentDto);

        verify(bookingRepository).exists(any(Specification.class));
    }

    @Test
    void findFirstByItemIdAndStartAfterOrderByStart() {
        long itemId = 1L;
        long userId = 1L;
        CommentDto commentDto = new CommentDto();
        User user = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(new Item()));
        when(bookingRepository.exists(any(Specification.class))).thenReturn(true);
        when(commentMapper.toComment(any(CommentDto.class), any(Item.class), any(User.class))).thenReturn(new Comment());
        when(commentRepository.save(any(Comment.class))).thenReturn(new Comment());

        itemService.createComment(itemId, userId, commentDto);

        verify(bookingRepository).exists(any(Specification.class));
    }
}