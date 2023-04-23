package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    List<Item> getAllItemsByUserId(Long userId);
    Optional<Item> getItemById(Long itemId);
    Item createItem(Item item);
    Item updateItem(Item item);
    void deleteItem(Long itemId);
    List<Item> searchItemsByText(String text);
}