package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class CommentRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentRepository commentRepository;

    private Item persistedItem;

    @BeforeEach
    public void setUp() {
        User user = User.builder()
                .name("test user")
                .email("test.user@example.com")
                .build();
        entityManager.persist(user);


        Item item = Item.builder()
                .name("test item")
                .description("test description")
                .owner(user)
                .available(true)
                .build();

        persistedItem = entityManager.persistAndFlush(item);

        Comment comment1 = Comment.builder()
                .text("test comment 1")
                .author(user)
                .item(item)
                .created(LocalDateTime.now())
                .build();

        Comment comment2 = Comment.builder()
                .text("test comment 2")
                .author(user)
                .item(item)
                .created(LocalDateTime.now())
                .build();

        entityManager.persist(comment1);
        entityManager.persist(comment2);
    }

    @Test
    void findByItemIdTest() {
        long itemId = persistedItem.getId();
        List<Comment> comments = commentRepository.findByItemId(itemId);

        assertNotNull(comments);
        assertEquals(2, comments.size());
        assertEquals("test comment 1", comments.get(0).getText());
        assertEquals("test comment 2", comments.get(1).getText());
    }
}