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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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

    @NotBlank(groups = {ItemMarker.New.class})
    @Size(groups = {ItemMarker.Update.class, ItemMarker.New.class}, min = 1)
    private String name;

    @NotBlank(groups = {ItemMarker.New.class})
    @Size(groups = {ItemMarker.Update.class, ItemMarker.New.class}, min = 1)
    private String description;

    @NotNull(groups = {ItemMarker.New.class})
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