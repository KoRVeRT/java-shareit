package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookingRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void saveBooking() {
        User owner = User.builder()
                .name("name 1")
                .email("name1@example.com")
                .build();
        entityManager.persist(owner);

        User booker = User.builder()
                .name("name 2")
                .email("name2@example.com")
                .build();
        entityManager.persist(booker);

        Item item = Item.builder()
                .name("item 1")
                .description("description 1")
                .available(true)
                .owner(owner)
                .build();
        entityManager.persist(item);

        Booking booking = Booking.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusHours(1))
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        entityManager.persist(booking);

        assertThat(booking.getId()).isNotNull();
        assertThat(booking.getStart()).isNotNull();
        assertThat(booking.getEnd()).isNotNull();
        assertThat(booking.getItem().getId()).isEqualTo(item.getId());
        assertThat(booking.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.WAITING);
    }
}