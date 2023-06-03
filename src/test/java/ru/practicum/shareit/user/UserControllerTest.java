package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllUsersTest() throws Exception {
        List<UserDto> users = Arrays.asList(
                UserDto.builder()
                        .id(1L).name("Dima Bill")
                        .email("dima_bill@example.ru")
                        .build(),
                UserDto.builder()
                        .id(2L).name("Kate Bill")
                        .email("kate_bill@example.ru")
                        .build()
        );

        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(users.size()))
                .andExpect(jsonPath("$[0].id").value(users.get(0).getId()))
                .andExpect(jsonPath("$[0].name").value(users.get(0).getName()))
                .andExpect(jsonPath("$[0].email").value(users.get(0).getEmail()))
                .andExpect(jsonPath("$[1].id").value(users.get(1).getId()))
                .andExpect(jsonPath("$[1].name").value(users.get(1).getName()))
                .andExpect(jsonPath("$[1].email").value(users.get(1).getEmail()));

        verify(userService, times(1)).getAllUsers();
        verifyNoMoreInteractions(userService);
    }

    @Test
    void getUserByIdTest() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(1L).name("Dima Bill")
                .email("dima_bill@example.ru")
                .build();

        when(userService.getUserById(userDto.getId())).thenReturn(userDto);

        mockMvc.perform(get("/users/{userId}", userDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.getId()))
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));

        verify(userService, times(1)).getUserById(userDto.getId());
        verifyNoMoreInteractions(userService);
    }

    @Test
    void createUserTest() throws Exception {
        UserDto userDto = UserDto.builder().name("Dima Bill").email("dima_bill@example.ru").build();
        UserDto createdUserDto = UserDto.builder().id(1L).name("Dima Bill").email("dima_bill@example.ru").build();

        when(userService.createUser(userDto)).thenReturn(createdUserDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdUserDto.getId()))
                .andExpect(jsonPath("$.name").value(createdUserDto.getName()))
                .andExpect(jsonPath("$.email").value(createdUserDto.getEmail()));

        verify(userService, times(1)).createUser(userDto);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void updateUserTest() throws Exception {
        UserDto existingUserDto = UserDto.builder()
                .id(1L)
                .name("Dima Bill")
                .email("dima_bill@example.ru")
                .build();
        UserDto updatedUserDto = UserDto.builder().id(1L)
                .name("Dima Bill Updated")
                .email("dima.bill.updated@example.ru")
                .build();

        when(userService.updateUser(existingUserDto)).thenReturn(updatedUserDto);

        mockMvc.perform(patch("/users/{userId}", existingUserDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedUserDto.getId()))
                .andExpect(jsonPath("$.name").value(updatedUserDto.getName()))
                .andExpect(jsonPath("$.email").value(updatedUserDto.getEmail()));

        verify(userService, times(1)).updateUser(existingUserDto);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void deleteUserTest() throws Exception {
        Long userId = 1L;

        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(userId);
        verifyNoMoreInteractions(userService);
    }
}