package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.repository.BookingSpecifications;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final BookingMapper bookingMapper;
    private final CommentMapper commentMapper;

    @Override
    public List<ItemResponseDto> getAllItemsByUserId(long userId) {
        findUserById(userId);
        log.info("Number of items in the list:{}", itemRepository.findByOwnerIdOrderById(userId).size());
        return itemRepository.findByOwnerIdOrderById(userId).stream()
                .map(item -> createItemResponseDto(item, userId))
                .collect(Collectors.toList());
    }

    @Override
    public ItemResponseDto getItemById(long itemId, long userId) {
        findUserById(userId);
        log.info("Getting item with id:{}", itemId);
        Item item = findItemById(itemId);
        return createItemResponseDto(item, userId);
    }

    @Transactional
    @Override
    public ItemDto createItem(ItemDto itemDto) {
        User user = findUserById(itemDto.getOwnerId());
        Item item = itemMapper.toItem(itemDto);
        item.setOwner(user);
        item = itemRepository.save(item);
        log.info("Created item with id:{}", item.getId());
        return itemMapper.toItemDto(item);
    }

    @Modifying
    @Transactional
    @Override
    public ItemDto updateItem(ItemDto itemDto) {
        Item item = findItemById(itemDto.getId());
        checkOwner(item, itemDto.getOwnerId());
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        item = itemRepository.save(item);
        log.info("Updated item with id:{}", item.getId());
        return itemMapper.toItemDto(item);
    }

    @Modifying
    @Transactional
    @Override
    public void deleteItem(long itemDtoId) {
        findItemById(itemDtoId);
        itemRepository.deleteById(itemDtoId);
        log.info("Deleted item with id:{}", itemDtoId);
    }

    @Override
    public List<ItemDto> searchItemsByText(String text) {
        log.info("Search item by user request:{}", text);
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return itemRepository.findItemsByText(text).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CommentDto createComment(long itemId, long userId, CommentDto commentDto) {
        User user = findUserById(userId);
        Item item = findItemById(itemId);
        LocalDateTime time = LocalDateTime.now();
        Specification<Booking> specification = BookingSpecifications.existsBookerIdAndItemIdAndEndBefore(
                userId, itemId, time);
        if (!bookingRepository.exists(specification)) {
            throw new ValidationException(String.format("User with id:%d didn't rent item with id:%d, " +
                    "or rent is still incomplete.", userId, itemId));
        }
        Comment comment = commentRepository.save(commentMapper.toComment(commentDto, item, user));
        log.info("Created comment with id:{}", comment.getId());
        return commentMapper.toCommentDto(comment);
    }

    private ItemResponseDto createItemResponseDto(Item item, long userId) {
        ItemResponseDto itemResponseDto = itemMapper.toItemResponseDto(item);
        if (item.getOwner().getId().equals(userId)) {
            bookingRepository.findAll(BookingSpecifications
                            .findFirstByItemIdAndStartBeforeOrderByStartDesc(
                                    item.getId(), LocalDateTime.now()), PageRequest.of(0, 1)).stream()
                    .findFirst()
                    .ifPresent(booking -> itemResponseDto.setLastBooking(bookingMapper.toBookingDto(booking)));
            bookingRepository.findAll(BookingSpecifications
                            .findFirstByItemIdAndStartAfterOrderByStart(
                                    item.getId(), LocalDateTime.now()), PageRequest.of(0, 1)).stream()
                    .findFirst()
                    .ifPresent(booking -> itemResponseDto.setNextBooking(bookingMapper.toBookingDto(booking)));
        }
        itemResponseDto.setComments(findCommentDtoByItemId(item.getId()));
        return itemResponseDto;
    }


    private List<CommentDto> findCommentDtoByItemId(long itemId) {
        return commentRepository.findByItemId(itemId)
                .stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    private User findUserById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id:%d not found", userId)));
    }

    private Item findItemById(long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Item with id:%d not found", itemId)));
    }

    private void checkOwner(Item item, Long userId) {
        if (!Objects.equals(item.getOwner().getId(), userId)) {
            throw new NotFoundException(String.format("Item with id: %d not owned by user with id: %d",
                    item.getId(), userId));
        }
    }
}