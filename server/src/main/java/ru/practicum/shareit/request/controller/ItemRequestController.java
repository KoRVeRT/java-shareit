package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;


@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;
    public static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @GetMapping
    public List<ItemRequestDto> getOwnRequestsByUserId(
            @RequestHeader(USER_ID_HEADER) long userId
    ) {
        log.info("Searching requests of userId={}", userId);
        return itemRequestService.getOwnRequestsByUserId(userId);
    }

    @PostMapping
    public ItemRequestDto createRequest(
            @RequestHeader(USER_ID_HEADER) long userId,
            @RequestBody ItemRequestDto itemRequestDto
    ) {
        log.info("Creating request {}, userId={}", itemRequestDto, userId);
        itemRequestDto.setRequestorId(userId);
        return itemRequestService.createRequest(itemRequestDto);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(
            @RequestHeader(USER_ID_HEADER) long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Get requests of other users, userId={}, from={}, size={}", userId, from, size);
        return itemRequestService.getAllRequests(from, size, userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(
            @PathVariable long requestId, @RequestHeader(USER_ID_HEADER) long userId
    ) {
        log.info("Get requestId={}, userId={}", requestId, userId);
        return itemRequestService.getRequestById(requestId, userId);
    }
}