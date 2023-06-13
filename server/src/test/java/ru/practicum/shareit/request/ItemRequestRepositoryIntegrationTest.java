package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ItemRequestRepositoryIntegrationTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Test
    void findByIdTest() {
        User user = userRepository.save(new User(1L, "user", "user@mail.ru"));
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("test description");
        itemRequest.setRequestor(user);
        itemRequest = entityManager.persist(itemRequest);

        Optional<ItemRequest> found = itemRequestRepository.findById(itemRequest.getId());

        assertTrue(found.isPresent());
        assertThat(itemRequest.getDescription(), equalTo(found.get().getDescription()));
        assertThat(itemRequest.getRequestor(), equalTo(user));
    }
}