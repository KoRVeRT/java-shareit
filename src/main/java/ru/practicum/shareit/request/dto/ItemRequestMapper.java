package ru.practicum.shareit.request.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserMapper;

@Component
@RequiredArgsConstructor
public class ItemRequestMapper {
    private final UserMapper userMapper;


    public ItemRequest toItemRequest(ItemRequestDto itemRequestDto) {
        return ItemRequest
                .builder()
                .description(itemRequestDto.getDescription())
                .build();
    }

    public ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return ItemRequestDto
                .builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requestor(userMapper.toUserDTO(itemRequest.getRequestor()))
                .created(itemRequest.getCreated())
                .build();
    }
}