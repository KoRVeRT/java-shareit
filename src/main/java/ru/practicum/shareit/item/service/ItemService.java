package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> getAllItemsByUserId(Long userId);

    ItemDto getItemById(Long itemId);

    ItemDto createItem(ItemDto itemDto);

    ItemDto updateItem(ItemDto itemDto);

    void deleteItem(Long itemId);

    List<ItemDto> searchItemsByText(String text);
}