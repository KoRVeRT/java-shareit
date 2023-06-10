package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;


import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    public static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @GetMapping
    public List<ItemDto> getAllItemsByUserId(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));
        return itemService.getAllItemsByUserId(userId, pageable);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable long itemId) {
        return itemService.getItemById(itemId, userId);
    }

    @PostMapping
    public ItemDto createItem(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestBody ItemDto itemDto) {
        itemDto.setOwnerId(userId);
        return itemService.createItem(itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestBody ItemDto itemDto,
            @PathVariable Long itemId) {
        itemDto.setId(itemId);
        itemDto.setOwnerId(userId);
        return itemService.updateItem(itemDto);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItemsByText(
            @RequestParam String text,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));
        return itemService.searchItemsByText(text, pageable);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable long itemId,
            @RequestBody CommentDto commentDto) {
        return itemService.createComment(itemId, userId, commentDto);
    }
}