package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;


import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class BookingDto {
    private Long id;

    private Long itemId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long bookerId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ItemDto item;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UserDto booker;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BookingStatus status;

    @JsonIgnore
    private boolean approved;

    private LocalDateTime start;

    private LocalDateTime end;
}