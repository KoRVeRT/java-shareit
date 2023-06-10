package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private MockMvc mockMvc;

    @Mock
    UserClient userClient;

    @InjectMocks
    private UserController userController;

    private static final String API_PREFIX = "/users";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @ParameterizedTest
    @MethodSource("provideInvalidUser")
    @SneakyThrows
    void createUser_shouldRespondWithBadRequest_ifUserIsInvalid(UserDto invalidUserDto) {
        String json = objectMapper.writeValueAsString(invalidUserDto);

        mockMvc.perform(post(API_PREFIX)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verifyNoInteractions(userClient);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidUpdateUser")
    @SneakyThrows
    void updateUser_shouldRespondWithBadRequest_ifUserIsInvalid(UserDto invalidUpdateUserDto) {
        String json = objectMapper.writeValueAsString(invalidUpdateUserDto);

        mockMvc.perform(patch(API_PREFIX + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verifyNoInteractions(userClient);
    }

    @SneakyThrows
    @Test
    void deleteUser_shouldReturnStatusNotFound_whenUserNotExists() {
        when(userClient.deleteUser(anyLong())).thenReturn(ResponseEntity.notFound().build());

        mockMvc.perform(delete(API_PREFIX + "/1"))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    void getUser_shouldReturnStatusNotFound_whenUserNotExists() {
        when(userClient.getUser(anyLong())).thenReturn(ResponseEntity.notFound().build());

        mockMvc.perform(get(API_PREFIX + "/1"))
                .andExpect(status().isNotFound());
    }

    private static Stream<Arguments> provideInvalidUser() {
        return Stream.of(
                Arguments.of(userDto(b -> b.setName(null))),
                Arguments.of(userDto(b -> b.setEmail(null))),
                Arguments.of(userDto(b -> b.setName(" "))),
                Arguments.of(userDto(b -> b.setEmail(" "))),
                Arguments.of(userDto(b -> b.setEmail("mail_mail.ru")))
        );
    }

    private static Stream<Arguments> provideInvalidUpdateUser() {
        return Stream.of(
                Arguments.of(userDto(b -> b.setName(" "))),
                Arguments.of(userDto(b -> b.setEmail(" "))),
                Arguments.of(userDto(b -> b.setEmail("mail?mail.ru")))
        );
    }

    private static UserDto userDto() {
        return UserDto.builder()
                .name("name")
                .email("mail@mail.com")
                .build();
    }

    private static UserDto userDto(Consumer<UserDto> consumer) {
        UserDto userDto = userDto();
        consumer.accept(userDto);
        return userDto;
    }
}
