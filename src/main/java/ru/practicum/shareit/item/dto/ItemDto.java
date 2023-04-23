package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode
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

    private Long requestId;
}
