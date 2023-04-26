package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Number of users in the list = {}", userRepository.getAllUsers().size());
        return userRepository.getAllUsers()
                .stream()
                .map(userMapper::toUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long userId) {
        log.info("Getting user with id = {}", userId);
        User user = userRepository.getUserById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id = %d not found", userId)));
        return userMapper.toUserDTO(user);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        checkIfEmailExists(userDto.getEmail());
        User newUser = userRepository.createUser(userMapper.toUser(userDto));
        log.info("Created user with id = {}", newUser.getId());
        return userMapper.toUserDTO(newUser);
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        User updatedUser = userMapper.toUser(getUserById(userDto.getId()));
        if (userDto.getName() != null) {
            updatedUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().equals(updatedUser.getEmail())) {
            checkIfEmailExists(userDto.getEmail());
            updatedUser.setEmail(userDto.getEmail());
        }
        updatedUser = userRepository.updateUser(updatedUser);
        log.info("Updated user with id = {}", updatedUser.getId());
        return userMapper.toUserDTO(updatedUser);
    }

    @Override
    public void deleteUser(Long userId) {
        getUserById(userId);
        userRepository.deleteUser(userId);
        log.info("Deleted user with id = {}", userId);
    }

    private void checkIfEmailExists(String email) {
        boolean emailExists = userRepository.getAllUsers()
                .stream()
                .map(User::getEmail)
                .anyMatch(userEmail -> userEmail.equals(email));

        if (emailExists) {
            throw new ConflictException(String.format("This \"%s\" email already exists", email));
        }
    }
}