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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest()
@TestPropertySource(properties = {"db.name=testItem"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ItemServiceIntegrationTest {
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
        assertNotNull(actual.getId(), "Created item's ID should not be null");
        assertEquals(expected.getName(),
                actual.getName(), "Created item's name should match the expected name");
        assertEquals(expected.getDescription(),
                actual.getDescription(), "Created item's description should match the expected description");
        assertEquals(expected.getAvailable(),
                actual.getAvailable(), "Created item's availability should match the expected availability");

        ItemDto itemDtoWithUserNotExist = itemDtoList.get(2).toBuilder().ownerId(100L).build();
        assertThrows(NotFoundException.class, () -> itemService.createItem(itemDtoWithUserNotExist),
                "NotFoundException should be thrown when trying to create an item with a non-existing user");

        ItemDto itemDtoWithEmptyName = itemDtoList.get(3).toBuilder().name(null).build();
        assertThrows(DataIntegrityViolationException.class,
                () -> itemService.createItem(itemDtoWithEmptyName),
                "Should be thrown when trying to create an item with a null name");
    }

    @Test
    void updateItem() {
        long ownerId = itemDtoList.get(2).getOwnerId();
        ItemDto current = itemService.createItem(itemDtoList.get(2));
        current.setOwnerId(ownerId);
        ItemDto updatedNameItem = current.toBuilder().name("updated").build();
        assertEquals(updatedNameItem, itemService.updateItem(updatedNameItem),
                "The updated item should match the expected updated item");

        ItemDto updatedOnlyOneField = ItemDto.builder().id(current.getId()).ownerId(ownerId).available(false).build();
        assertEquals(updatedNameItem.toBuilder().available(false).build(), itemService.updateItem(updatedOnlyOneField),
                "The updated item with only one field updated should match the expected updated item");

        ItemDto itemDtoWithIdNotExists = current.toBuilder().id(100L).build();
        assertThrows(NotFoundException.class, () -> itemService.updateItem(itemDtoWithIdNotExists),
                "NotFoundException should be thrown when trying to update a non-existing item");

        ItemDto updateItemDtoWithNotOwnerId = current.toBuilder().ownerId(ownerId + 10L).build();
        assertThrows(NotFoundException.class, () -> itemService.updateItem(updateItemDtoWithNotOwnerId),
                "NotFoundException should be thrown when trying to update an item by a non-owner");
    }

    @Test
    void getItemById() {
        long userId = itemDtoList.get(3).getOwnerId();
        ItemDto expected = itemService.createItem(itemDtoList.get(3));
        ItemDto actual = itemService.getItemById(expected.getId(), userId);
        assertEquals(expected.getName(), actual.getName(),
                "The fetched item's name should match the expected name");
        assertEquals(expected.getDescription(), actual.getDescription(),
                "The fetched item's description should match the expected description");
        assertEquals(expected.getAvailable(), actual.getAvailable(),
                "The fetched item's availability should match the expected availability");
        assertNotNull(actual.getComments(), "The fetched item's comments should not be null");
        assertTrue(actual.getComments().isEmpty(), "The fetched item's comments should be empty");
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

        ItemDto itemWithNotApproval = itemService.getItemById(itemDto.getId(), ownerId);
        assertNull(itemWithNotApproval.getLastBooking(),
                "The last booking should be null for the item fetched by ID before approval");

        BookingDto updateBookingDto = lastBooking.toBuilder().bookerId(ownerId).id(lastBookingId).approved(true).build();
        bookingService.updateBooking(updateBookingDto);
        assertEquals(nextBookingId, itemService.getItemById(itemDto.getId(), ownerId).getNextBooking().getId(),
                "Last booking should be equal for the item after the last booking has been approved");
        assertEquals(lastBookingId, itemService.getItemById(itemDto.getId(), ownerId).getLastBooking().getId(),
                "Last booking should be equal for the item because it is not rejected");

        assertNull(itemService.getItemById(itemDto.getId(), ownerId + 1).getLastBooking(),
                "The last booking should be null for the item fetched by ID when fetched by a non-owner");
        assertNull(itemService.getItemById(itemDto.getId(), ownerId + 1).getNextBooking(),
                "The next booking should be null for the item fetched by ID when fetched by a non-owner");
    }


    @Test
    void getAllItemsByUserId() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        long userId = 4L;

        assertTrue(itemService.getAllItemsByUserId(userId, pageable).isEmpty());

        ItemDto itemDto1 = itemService.createItem(itemDtoList.get(4));
        List<ItemDto> expected = new ArrayList<>(List.of(itemDto1));
        List<ItemDto> byUserIdOneItem = itemService.getAllItemsByUserId(userId, pageable);
        assertFalse(byUserIdOneItem.isEmpty(), "The fetched items by user ID should not be empty");
        assertEquals(1, expected.size(),
                "The size of expected items should match the size of fetched items");
        assertEquals(expected.get(0).getId(), byUserIdOneItem.get(0).getId(),
                "The first fetched item's ID should match the expected item's ID");

        ItemDto itemDto2 = itemService.createItem(itemDtoList.get(4));
        expected.add(itemDto2);
        List<ItemDto> byUserIdTwoItem = itemService.getAllItemsByUserId(userId, pageable);
        assertFalse(byUserIdTwoItem.isEmpty(), "The fetched items by user ID should not be empty");
        assertEquals(2, expected.size(),
                "The size of expected items should match the size of fetched items");
        assertEquals(expected.get(0).getId(), byUserIdOneItem.get(0).getId(),
                "The first fetched item's ID should match the expected item's ID");
        assertEquals(expected.get(1).getId(), byUserIdTwoItem.get(1).getId(),
                "The second fetched item's ID should match the expected item's ID");
    }

    @Test
    void searchItemsByText() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        ItemDto itemDto = itemService.createItem(itemDtoList.get(6));

        List<ItemDto> foundItemDtoByDescription = itemService.searchItemsByText("6 deSc", pageable);
        assertTrue(foundItemDtoByDescription.contains(itemDto),
                "Search result by description should contain expected item");
        assertEquals(1, foundItemDtoByDescription.size(),
                "Size of the search results by description should be 1");

        itemDto.setDescription("updated");
        itemService.updateItem(itemDto.toBuilder().ownerId(6L).description("updated").build());
        List<ItemDto> foundItemDtoByName = itemService.searchItemsByText("item6", pageable);
        assertTrue(foundItemDtoByName.contains(itemDto), "Search result by name should contain expected item");
        assertEquals(1, foundItemDtoByName.size(), "Size of the search results by name should be 1");

        assertTrue(itemService.searchItemsByText("exit", pageable).isEmpty(),
                "The search results should be empty when the search text does not match any items");

        assertTrue(itemService.searchItemsByText(" ", pageable).isEmpty(),
                "The search results should be empty when the search text blank");

        itemService.updateItem(itemDto.toBuilder().ownerId(6L).available(false).build());
        assertTrue(itemService.searchItemsByText("Item6", pageable).isEmpty(),
                "The search results should be empty when item not available");
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
        BookingDto bookingResponseDto = bookingService.createBooking(bookingDto);

        bookingService.updateBooking(bookingDto.toBuilder()
                .id(bookingResponseDto.getId())
                .bookerId(ownerItemId)
                .approved(true)
                .build());

        CommentDto commentDto = CommentDto.builder()
                .text("TextComment")
                .build();
        CommentDto actual = itemService.createComment(itemId, authorId, commentDto);

        assertEquals(commentDto.getText(), actual.getText(),
                "The created comment's text should match the expected text");
        assertEquals(userService.getUserById(authorId).getName(), actual.getAuthorName(),
                "The created comment's author name should match the expected author name");
        assertNotNull(actual.getCreated(), "The created comment's creation date should not be null");
        assertNotEquals(0, actual.getId(), "The created comment's ID should not be 0");
    }
}