package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
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
        log.info("Get items of userId={}, from={}, size={}", userId, from, size);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));
        return itemService.getAllItemsByUserId(userId, pageable);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable long itemId) {
        log.info("Get itemId={} by userId={}", itemId, userId);
        return itemService.getItemById(itemId, userId);
    }

    @PostMapping
    public ItemDto createItem(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestBody ItemDto itemDto) {
        itemDto.setOwnerId(userId);
        log.info("Creating item {}, userId={}", itemDto, userId);
        return itemService.createItem(itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestBody ItemDto itemDto,
            @PathVariable Long itemId) {
        log.info("Update itemId={} by userId={}", itemId, userId);
        itemDto.setId(itemId);
        itemDto.setOwnerId(userId);
        return itemService.updateItem(itemDto);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItemsByText(
            @RequestHeader(USER_ID_HEADER) long userId,
            @RequestParam String text,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Get items with text \"{}\", userId={}, from={}, size={}", text, userId, from, size);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));
        return itemService.searchItemsByText(userId, text, pageable);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable long itemId,
            @RequestBody CommentDto commentDto) {
        log.info("Creating comment \"{}\" for itemId={} by userId={}", commentDto.getText(), itemId, userId);
        return itemService.createComment(itemId, userId, commentDto);
    }
}