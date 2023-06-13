package ru.practicum.shareit.user.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.constraints.NotBlankOrNull;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {
    Long id;

    @NotBlank(groups = Creat.class)
    @Size(groups = {Creat.class, Update.class}, max = 255, min = 5)
    @NotBlankOrNull(groups = Update.class)
    @Email(groups = {Creat.class, Update.class})
    String email;

    @NotBlank(groups = Creat.class)
    @NotBlankOrNull(groups = Update.class)
    @Size(groups = {Creat.class, Update.class}, max = 255, min = 1)
    String name;

    public interface Creat {
    }

    public interface Update {
    }
}
