package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.booking.dto.BookingDto;


import java.util.List;

@Builder(toBuilder = true)
@Getter
@Setter
@ToString
@EqualsAndHashCode(exclude = {"ownerId"})
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private Long id;

    private String name;

    private String description;


    private Boolean available;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long ownerId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long requestId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<CommentDto> comments;

    private BookingDto lastBooking;
    private BookingDto nextBooking;
}