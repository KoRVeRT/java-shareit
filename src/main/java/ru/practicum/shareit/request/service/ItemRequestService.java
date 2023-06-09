package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createRequest(ItemRequestDto itemRequestDto);

    List<ItemRequestDto> getOwnRequestsByUserId(long userId);

    List<ItemRequestDto> getAllRequests(int from, int size, long userId);

    ItemRequestDto getRequestById(long requestId, long userId);
}