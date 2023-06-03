package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class ItemRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ItemRepository itemRepository;

    private User persistedUser;

    @BeforeEach
    public void setUp() {
        User user = User.builder()
                .name("Test User")
                .email("test.user@example.com")
                .build();

        persistedUser = entityManager.persistAndFlush(user);

        Item item1 = Item.builder()
                .name("Test Item 1")
                .description("Test Description 1")
                .owner(user)
                .available(true)
                .build();

        Item item2 = Item.builder()
                .name("Test Item 2")
                .description("Test Description 2")
                .owner(user)
                .available(true)
                .build();

        entityManager.persist(item1);
        entityManager.persist(item2);
    }

    @Test
    void findAllByOwnerIdTest() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Item> items = itemRepository.findAllByOwnerId(persistedUser.getId(), pageable);

        assertNotNull(items);
        assertEquals(2, items.size());
    }

    @Test
    void findItemsByTextTest() {
        String text = "test";
        Pageable pageable = PageRequest.of(0, 10);
        List<Item> items = itemRepository.findItemsByText(text, pageable);

        assertNotNull(items);
        assertEquals(2, items.size());
    }

    @Test
    void findAllByRequestIdTest() {
        long requestId = 1L;
        List<Item> items = itemRepository.findAllByRequestId(requestId);
        assertNotNull(items);
    }
}