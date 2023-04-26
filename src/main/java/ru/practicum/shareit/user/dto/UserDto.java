package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;

    @NotBlank(groups = {UserMarker.New.class})
    @Size(groups = {UserMarker.New.class, UserMarker.Update.class}, min = 1)
    private String name;

    @NotBlank(groups = {UserMarker.New.class})
    @Size(groups = {UserMarker.Update.class, UserMarker.New.class}, min = 5)
    @Email(groups = {UserMarker.New.class, UserMarker.Update.class})
    private String email;
}