package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequestDto {
    private Long id;

    private String description;

    private UserDto requestor;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long requestorId;

    private LocalDateTime created;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ItemDto> items;
}