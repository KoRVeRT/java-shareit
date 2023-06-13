package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Slf4j
@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemClient itemClient;
    public static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    @Validated(ItemDto.Create.class)
    public ResponseEntity<Object> postItem(@RequestBody @Validated(ItemDto.Create.class) ItemDto itemDto,
                                           @RequestHeader(USER_ID_HEADER) @Positive long userId) {
        log.info("Creating item {}, userId={}", itemDto, userId);
        return itemClient.postItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestBody @Valid ItemDto itemDto,
                                             @PathVariable @Positive Long itemId,
                                             @RequestHeader(USER_ID_HEADER) @Positive long userId) {
        log.info("Update itemId={} by userId={}", itemId, userId);
        return itemClient.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@PathVariable @Positive Long itemId,
                                          @RequestHeader(USER_ID_HEADER) @Positive long userId) {
        log.info("Get itemId={} by userId={}", itemId, userId);
        return itemClient.getItem(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItems(
            @RequestHeader(USER_ID_HEADER) @Positive long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero(message = "from cannot be negative") int from,
            @RequestParam(defaultValue = "10") @Positive(message = "size must be positive") int size
    ) {
        log.info("Get items of userId={}, from={}, size={}", userId, from, size);
        return itemClient.getAllItems(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> findByText(
            @RequestHeader(USER_ID_HEADER) @Positive long userId,
            @RequestParam String text,
            @RequestParam(defaultValue = "0") @PositiveOrZero(message = "from cannot be negative") int from,
            @RequestParam(defaultValue = "10") @Positive(message = "size must be positive") int size
    ) {
        log.info("Get items with text \"{}\", userId={}, from={}, size={}", text, userId, from, size);
        return itemClient.findByText(userId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> postComment(@RequestHeader(USER_ID_HEADER) @Positive Long authorId,
                                              @PathVariable @Positive long itemId,
                                              @Valid @RequestBody CommentDto commentDto) {

        log.info("Creating comment \"{}\" for itemId={} by userId={}", commentDto.getText(), itemId, authorId);
        return itemClient.postComment(authorId, itemId, commentDto);
    }

}
