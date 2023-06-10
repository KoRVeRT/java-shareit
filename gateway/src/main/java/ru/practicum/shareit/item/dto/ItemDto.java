package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.constraints.NotBlankOrNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    Long id;

    @NotNull(groups = Create.class)
    Boolean available;

    @NotBlank(groups = Create.class)
    @NotBlankOrNull
    @Size(max = 255)
    String name;

    @NotBlank(groups = Create.class)
    @NotBlankOrNull
    @Size(max = 512)
    String description;

    Long requestId;

    public interface Create {
    }

}