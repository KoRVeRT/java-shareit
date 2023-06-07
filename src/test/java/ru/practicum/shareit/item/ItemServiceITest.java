package ru.practicum.shareit.item;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest()
@TestPropertySource(properties = {"db.name=testItem"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ItemServiceITest {
    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;
    private static List<ItemDto> itemDtoList;

    @BeforeAll
    void before() {
        LongStream.rangeClosed(0, 10)
                .mapToObj((i) -> UserDto.builder()
                        .name(String.format("User%d", i))
                        .email(String.format("email%d@mail.net", i))
                        .build()).forEach(userService::createUser);

        itemDtoList = LongStream.rangeClosed(0, 10)
                .mapToObj((i) -> ItemDto.builder()
                        .ownerId(i)
                        .available(true)
                        .name(String.format("Item%d", i))
                        .description(String.format("Item%d description", i))
                        .comments(new ArrayList<>())
                        .build()).collect(Collectors.toList());
    }

    @Test
    void createItem() {
        ItemDto actual = itemService.createItem(itemDtoList.get(1));
        ItemDto expected = itemDtoList.get(1);
        assertNotNull(actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getAvailable(), actual.getAvailable());

        //user not exists
        ItemDto itemDtoWithUserNotExist = itemDtoList.get(2).toBuilder().ownerId(100L).build();
        assertThrows(NotFoundException.class, () -> itemService.createItem(itemDtoWithUserNotExist));

        //empty name of dto
        ItemDto itemDtoWithEmptyName = itemDtoList.get(3).toBuilder().name(null).build();
        assertThrows(DataIntegrityViolationException.class,
                () -> itemService.createItem(itemDtoWithEmptyName));
    }

    @Test
    void updateItem() {
        long ownerId = itemDtoList.get(2).getOwnerId();
        ItemDto current = itemService.createItem(itemDtoList.get(2));
        current.setOwnerId(ownerId);
        ItemDto updatedNameItem = current.toBuilder().name("updated").build();
        assertEquals(updatedNameItem, itemService.updateItem(updatedNameItem));

        // update only available field
        ItemDto updatedOnlyOneField = ItemDto.builder().id(current.getId()).ownerId(ownerId).available(false).build();
        assertEquals(updatedNameItem.toBuilder().available(false).build(), itemService.updateItem(updatedOnlyOneField));

        // item not exists
        ItemDto itemDtoWithIdNotExists = current.toBuilder().id(100L).build();
        assertThrows(NotFoundException.class, () -> itemService.updateItem(itemDtoWithIdNotExists));

        //update by not owner
        ItemDto updateItemDtoWithNotOwnerId = current.toBuilder().ownerId(ownerId + 10L).build();
        assertThrows(NotFoundException.class, () -> itemService.updateItem(updateItemDtoWithNotOwnerId));
    }

    @Test
    void getItemById() {
        long userId = itemDtoList.get(3).getOwnerId();
        ItemDto expected = itemService.createItem(itemDtoList.get(3));
        ItemDto actual = itemService.getItemById(expected.getId(), userId);
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getAvailable(), actual.getAvailable());
        assertNotNull(actual.getComments());
        assertTrue(actual.getComments().isEmpty());
    }

    @Test
    void getByItemIdWithBookings() {
        ItemDto itemDto = itemService.createItem(itemDtoList.get(1));
        long itemId = itemDto.getId();
        long ownerId = 1L;
        assertEquals(itemId, itemService.getItemById(itemDto.getId(), ownerId).getId());
        assertNull(itemService.getItemById(itemDto.getId(), ownerId).getLastBooking());
        assertNull(itemService.getItemById(itemDto.getId(), ownerId).getNextBooking());

        long bookerId = 2L;
        BookingDto lastBooking = BookingDto.builder()
                .itemId(itemId)
                .bookerId(bookerId)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().plusDays(1))
                .build();
        BookingDto nextBooking = BookingDto.builder()
                .itemId(itemId)
                .bookerId(bookerId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(6))
                .build();
        long lastBookingId = bookingService.createBooking(lastBooking).getId();
        long nextBookingId = bookingService.createBooking(nextBooking).getId();

        // without approval last booking
        ItemDto itemWithNotApproval = itemService.getItemById(itemDto.getId(), ownerId);
        assertNull(itemWithNotApproval.getLastBooking());

        // approval last booking
        BookingDto updateBookingDto = lastBooking.toBuilder().bookerId(ownerId).id(lastBookingId).approved(true).build();
        bookingService.updateBooking(updateBookingDto);
        assertEquals(nextBookingId, itemService.getItemById(itemDto.getId(), ownerId).getNextBooking().getId());
        assertEquals(lastBookingId, itemService.getItemById(itemDto.getId(), ownerId).getLastBooking().getId());

        //id not owner by item
        assertNull(itemService.getItemById(itemDto.getId(), ownerId + 1).getLastBooking());
        assertNull(itemService.getItemById(itemDto.getId(), ownerId + 1).getNextBooking());
    }


    @Test
    void getAllItemsByUserId() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        long userId = 4L;

        //no items
        assertTrue(itemService.getAllItemsByUserId(userId, pageable).isEmpty());

        //one item
        ItemDto itemDto1 = itemService.createItem(itemDtoList.get(4));
        List<ItemDto> expected = new ArrayList<>(List.of(itemDto1));
        List<ItemDto> byUserIdOneItem = itemService.getAllItemsByUserId(userId, pageable);
        assertFalse(byUserIdOneItem.isEmpty());
        assertEquals(1, expected.size());
        assertEquals(expected.get(0).getId(), byUserIdOneItem.get(0).getId());

        //two items
        ItemDto itemDto2 = itemService.createItem(itemDtoList.get(4));
        expected.add(itemDto2);
        List<ItemDto> byUserIdTwoItem = itemService.getAllItemsByUserId(userId, pageable);
        assertFalse(byUserIdTwoItem.isEmpty());
        assertEquals(2, expected.size());
        assertEquals(expected.get(0).getId(), byUserIdOneItem.get(0).getId());
        assertEquals(expected.get(1).getId(), byUserIdTwoItem.get(1).getId());
    }

    @Test
    void searchItemsByText() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        ItemDto itemDto = itemService.createItem(itemDtoList.get(6));
        System.out.println(itemService.getItemById(itemDto.getId(), 6));

        //search in description with caseInsensitive
        List<ItemDto> foundItemDtoByDescription = itemService.searchItemsByText("6 deSc", pageable);
        assertTrue(foundItemDtoByDescription.contains(itemDto));
        assertEquals(1, foundItemDtoByDescription.size());

        //search in name with caseInsensitive
        itemDto.setDescription("updated");
        itemService.updateItem(itemDto.toBuilder().ownerId(6L).description("updated").build());
        List<ItemDto> foundItemDtoByName = itemService.searchItemsByText("item6", pageable);
        assertTrue(foundItemDtoByName.contains(itemDto));
        assertEquals(1, foundItemDtoByName.size());

        //if items not found
        assertTrue(itemService.searchItemsByText("exit", pageable).isEmpty());

        //search with blank string and return empty list
        assertTrue(itemService.searchItemsByText(" ", pageable).isEmpty());

        //if item not available
        itemService.updateItem(itemDto.toBuilder().ownerId(6L).available(false).build());
        assertTrue(itemService.searchItemsByText("Item6", pageable).isEmpty());
    }

    @Test
    void createComment() {
        ItemDto dto = itemDtoList.get(7);
        long authorId = 7L;
        long ownerItemId = 6L;
        ItemDto expected = itemService.createItem(dto.toBuilder().ownerId(ownerItemId).build());
        long itemId = expected.getId();
        BookingDto bookingDto = BookingDto.builder()
                .bookerId(authorId)
                .itemId(itemId)
                .start(LocalDateTime.now().minusMinutes(1).withNano(0))
                .end(LocalDateTime.now().withNano(0))
                .build();
        BookingResponseDto bookingResponseDto = bookingService.createBooking(bookingDto);
        // approve booking for comment
        bookingService.updateBooking(bookingDto.toBuilder()
                .id(bookingResponseDto.getId())
                .bookerId(ownerItemId)
                .approved(true)
                .build());

        CommentDto commentDto = CommentDto.builder()
                .text("TextComment")
                .build();
        CommentDto actual = itemService.createComment(itemId, authorId, commentDto);

        assertEquals(commentDto.getText(), actual.getText());
        assertEquals(userService.getUserById(authorId).getName(), actual.getAuthorName());
        assertNotNull(actual.getCreated());
        assertNotEquals(0, actual.getId());
    }
}