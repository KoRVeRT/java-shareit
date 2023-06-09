package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;


@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;
    public static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @GetMapping
    public List<ItemRequestDto> getOwnRequestsByUserId(
            @RequestHeader(USER_ID_HEADER) long userId
    ) {
        return itemRequestService.getOwnRequestsByUserId(userId);
    }

    @PostMapping
    public ItemRequestDto createRequest(
            @RequestHeader(USER_ID_HEADER) long userId,
            @Valid @RequestBody ItemRequestDto itemRequestDto
    ) {
        itemRequestDto.setRequestorId(userId);
        return itemRequestService.createRequest(itemRequestDto);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(
            @RequestHeader(USER_ID_HEADER) long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size
    ) {
        return itemRequestService.getAllRequests(from, size, userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(
            @PathVariable long requestId, @RequestHeader(USER_ID_HEADER) long userId
    ) {
        return itemRequestService.getRequestById(requestId, userId);
    }
}