package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.persistence.criteria.Predicate;
import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    private final ItemRequestRepository itemRequestRepository;
    private final ItemMapper itemMapper;
    private final BookingMapper bookingMapper;
    private final CommentMapper commentMapper;
    private static final String BOOKING_START_DATE_FIELD_NAME = "start";
    private static final String BOOKING_END_DATE_FIELD_NAME = "end";
    private static final String ID_FIELD_NAME = "id";
    private static final String BOOKING_ITEM_FIELD_NAME = "item";
    private static final String BOOKING_BOOKER_FIELD_NAME = "booker";
    private static final String BOOKING_STATUS_FIELD_NAME = "status";

    @Override
    public List<ItemDto> getAllItemsByUserId(long userId, Pageable pageable) {
        findUserById(userId);
        return itemRepository.findAllByOwnerId(userId, pageable).stream()
                .map(item -> createItemDto(item, userId))
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getItemById(long itemId, long userId) {
        findUserById(userId);
        log.info("Getting item with id:{}", itemId);
        Item item = findItemById(itemId);
        return createItemDto(item, userId);
    }

    @Transactional
    @Override
    public ItemDto createItem(ItemDto itemDto) {
        User user = findUserById(itemDto.getOwnerId());
        Item item = itemMapper.toItem(itemDto);
        item.setOwner(user);
        item.setRequest(findRequest(itemDto).orElse(null));
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
        item.setRequest(findRequest(itemDto).orElse(null));
        item = itemRepository.save(item);
        log.info("Updated item with id:{}", item.getId());
        return itemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> searchItemsByText(long userId, String text, Pageable pageable) {
        findUserById(userId);
        log.info("Search item by user request:{}", text);
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return itemRepository.findItemsByText(text, pageable).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CommentDto createComment(long itemId, long userId, CommentDto commentDto) {
        User user = findUserById(userId);
        Item item = findItemById(itemId);
        LocalDateTime time = LocalDateTime.now();
        Specification<Booking> specification = existsBookerIdAndItemIdAndEndBefore(
                userId, itemId, time);
        if (!bookingRepository.exists(specification)) {
            throw new ValidationException(String.format("User with id:%d didn't rent item with id:%d, " +
                    "or rent is still incomplete.", userId, itemId));
        }
        Comment comment = commentRepository.save(commentMapper.toComment(commentDto, item, user));
        log.info("Created comment with id:{}", comment.getId());
        return commentMapper.toCommentDto(comment);
    }

    private ItemDto createItemDto(Item item, long userId) {
        ItemDto itemDto = itemMapper.toItemDto(item);
        if (item.getOwner().getId().equals(userId)) {
            bookingRepository.findAll(findFirstByItemIdAndStartBeforeOrderByStartDesc(
                            item.getId(), LocalDateTime.now()), PageRequest.of(0, 1)).stream()
                    .findFirst()
                    .ifPresent(booking -> itemDto.setLastBooking(bookingMapper.toBookingDto(booking)));
            bookingRepository.findAll(findFirstByItemIdAndStartAfterOrderByStart(
                            item.getId(), LocalDateTime.now()), PageRequest.of(0, 1)).stream()
                    .findFirst()
                    .ifPresent(booking -> itemDto.setNextBooking(bookingMapper.toBookingDto(booking)));
        }
        itemDto.setComments(findCommentDtoByItemId(item.getId()));
        return itemDto;
    }

    private Specification<Booking> existsBookerIdAndItemIdAndEndBefore(
            Long bookerId, Long itemId, LocalDateTime time) {
        return (root, query, builder) -> {
            Predicate bookerIdPredicate = builder.equal(root.get(BOOKING_BOOKER_FIELD_NAME).get(ID_FIELD_NAME),
                    bookerId);
            Predicate itemIdPredicate = builder.equal(root.get(BOOKING_ITEM_FIELD_NAME).get(ID_FIELD_NAME),
                    itemId);
            Predicate endPredicate = builder.lessThan(root.get(BOOKING_END_DATE_FIELD_NAME), time);
            Predicate statusPredicate = builder.equal(root.get(BOOKING_STATUS_FIELD_NAME), BookingStatus.APPROVED);
            return builder.and(bookerIdPredicate, itemIdPredicate, endPredicate, statusPredicate);
        };
    }

    private Specification<Booking> findFirstByItemIdAndStartBeforeOrderByStartDesc(
            Long itemId, LocalDateTime time) {
        return (root, query, builder) -> {
            Predicate itemIdPredicate = builder.equal(root.get(BOOKING_ITEM_FIELD_NAME).get(ID_FIELD_NAME),
                    itemId);
            Predicate startPredicate = builder.lessThan(root.get(BOOKING_START_DATE_FIELD_NAME), time);
            Predicate statusPredicate = builder.equal(root.get(BOOKING_STATUS_FIELD_NAME), BookingStatus.APPROVED);
            query.orderBy(builder.desc(root.get(BOOKING_START_DATE_FIELD_NAME)));
            return builder.and(itemIdPredicate, startPredicate, statusPredicate);
        };
    }

    private Specification<Booking> findFirstByItemIdAndStartAfterOrderByStart(
            Long itemId, LocalDateTime time) {
        return (root, query, builder) -> {
            Predicate itemIdPredicate = builder.equal(root.get(BOOKING_ITEM_FIELD_NAME).get(ID_FIELD_NAME),
                    itemId);
            Predicate startPredicate = builder.greaterThan(root.get(BOOKING_START_DATE_FIELD_NAME), time);
            Predicate statusPredicate = builder.notEqual(root.get(BOOKING_STATUS_FIELD_NAME), BookingStatus.REJECTED);
            query.orderBy(builder.asc(root.get(BOOKING_START_DATE_FIELD_NAME)));
            return builder.and(itemIdPredicate, startPredicate, statusPredicate);
        };
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

    private Optional<ItemRequest> findRequest(ItemDto itemDto) {
        if (itemDto.getRequestId() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(itemRequestRepository.findById(itemDto.getRequestId())
                .orElseThrow(() -> new NotFoundException(String.format("Request with id:%d not found",
                        itemDto.getRequestId()))));
    }

    private void checkOwner(Item item, Long userId) {
        if (!Objects.equals(item.getOwner().getId(), userId)) {
            throw new NotFoundException(String.format("Item with id: %d not owned by user with id: %d",
                    item.getId(), userId));
        }
    }
}