package ru.practicum.shareit.item.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> getAllItemsByUserId(long userId, Pageable pageable);

    ItemDto getItemById(long itemId, long userId);

    ItemDto createItem(ItemDto itemDto);

    ItemDto updateItem(ItemDto itemDto);

    List<ItemDto> searchItemsByText(long userId, String text, Pageable pageable);

    CommentDto createComment(long itemId, long userId, CommentDto commentDto);
}