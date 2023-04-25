package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ItemRepositoryImpl implements ItemRepository {
    private long id = 1L;
    private final Map<Long, Item> items = new HashMap<>();

    @Override
    public List<Item> getAllItemsByUserId(Long userId) {
        return List.copyOf(items.values().stream()
                .filter(item -> Objects.equals(item.getOwner().getId(), userId))
                .collect(Collectors.toList()));
    }

    @Override
    public Optional<Item> getItemById(Long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public Item createItem(Item item) {
        item.setId(id++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public void deleteItem(Long itemId) {
        items.remove(itemId);
    }

    @Override
    public List<Item> searchItemsByText(String text) {
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> isSearchText(item, text))
                .collect(Collectors.toList());
    }

    private Boolean isSearchText(Item item, String text) {
        return item.getName().toLowerCase().contains(text.toLowerCase())
                || item.getDescription().toLowerCase().contains(text.toLowerCase());
    }
}