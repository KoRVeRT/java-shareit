package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> getAllItemsByUserId(Long userId);

    ItemDto getItemById(Long itemDtoId);

    ItemDto createItem(ItemDto itemDto, Long userId);

    ItemDto updateItem(ItemDto itemDto, Long itemId, Long ownerId);

    void deleteItem(Long itemDtoId);

    List<ItemDto> searchItemsByText(String text);
}