package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Spy
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    public void beforeEach() {
        user = new User(1L, "Name", "name@mail.ru");
    }

    @Test
    void createUserTest() {
        UserDto savedUser = userMapper.toUserDto(user);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto newUser = userService.createUser(savedUser);

        assertEquals(1L, newUser.getId());
        assertEquals(savedUser.getName(), newUser.getName());
        assertEquals(savedUser.getEmail(), newUser.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_ReturnsUpdatedUser_WhenUserExists() {
        String updatedName = "updated name";
        user.setName(updatedName);

        UserDto updateDto = userMapper.toUserDto(user);

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto savedDto = userService.updateUser(updateDto);

        assertThat(savedDto.getId(), equalTo(1L));
        assertThat(savedDto.getName(), equalTo(updatedName));
        verify(userRepository, times(1)).findById(any(Long.class));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void findByIdTest_UserExists() {
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));

        UserDto userDto = userService.getUserById(1L);

        assertThat(userDto.getId(), equalTo(1L));
        assertThat(userDto.getName(), equalTo("Name"));
        assertThat(userDto.getEmail(), equalTo("name@mail.ru"));
        verify(userRepository, times(1)).findById(any(Long.class));
    }

    @Test
    void findByIdTest_UserNotExists() {
        assertThrows(NotFoundException.class, () -> userService.getUserById(1L));

        verify(userRepository, times(1)).findById(any(Long.class));
    }


    @Test
    void deleteByIdTest() {
        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void getAllUsersTest() {
        when(userRepository.findAll()).thenReturn(List.of(user, user, user));

        List<UserDto> users = userService.getAllUsers();

        assertThat(3, equalTo(users.size()));
        assertThat(users.get(0).getId(), equalTo(user.getId()));
        verify(userRepository, times(1)).findAll();
    }
}