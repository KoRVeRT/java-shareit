package ru.practicum.shareit.item.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.util.List;

public interface ItemService {
    List<ItemResponseDto> getAllItemsByUserId(long userId, Pageable pageable);

    ItemResponseDto getItemById(long itemId, long userId);

    ItemDto createItem(ItemDto itemDto);

    ItemDto updateItem(ItemDto itemDto);

    void deleteItem(long itemId);

    List<ItemDto> searchItemsByText(String text, Pageable pageable);

    CommentDto createComment(long itemId, long userId, CommentDto commentDto);
}