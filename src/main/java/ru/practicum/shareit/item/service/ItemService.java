package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.util.List;

public interface ItemService {
    List<ItemResponseDto> getAllItemsByUserId(long userId);

    ItemResponseDto getItemById(long itemId, long userId);

    ItemDto createItem(ItemDto itemDto);

    ItemDto updateItem(ItemDto itemDto);

    void deleteItem(long itemId);

    List<ItemDto> searchItemsByText(String text);
    CommentDto createComment(long itemId, long userId, CommentDto commentDto);
}