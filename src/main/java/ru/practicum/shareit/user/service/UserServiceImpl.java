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

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Number of users in the list = {}", userRepository.getAllUsers().size());
        return userRepository.getAllUsers()
                .stream()
                .map(UserMapper::toUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long userId) {
        log.info("Getting user with id = {}", userId);
        User user = userRepository.getUserById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id = %d not found", userId)));
        return UserMapper.toUserDTO(user);
    }

    @Override
    public UserDto createUser(UserDto userDTO) {
        checkEmailForExist(userDTO.getEmail());
        User newUser = userRepository.createUser(UserMapper.toUser(userDTO));
        log.info("Created user with id = {}", newUser.getId());
        return UserMapper.toUserDTO(newUser);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDTO) {
        User updatedUser = UserMapper.toUser(getUserById(userId));
        if (userDTO.getName() != null) {
            updatedUser.setName(userDTO.getName());
        }
        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(updatedUser.getEmail())) {
            checkEmailForExist(userDTO.getEmail());
            updatedUser.setEmail(userDTO.getEmail());
        }
        updatedUser = userRepository.updateUser(updatedUser);
        log.info("Updated user with id = {}", updatedUser.getId());
        return UserMapper.toUserDTO(updatedUser);
    }

    @Override
    public void deleteUser(Long userId) {
        getUserById(userId);
        userRepository.deleteUser(userId);
        log.info("Deleted user with id = {}", userId);
    }

    private void checkEmailForExist(String email) {
        boolean isExistsEmail = userRepository.getAllUsers()
                .stream()
                .map(User::getEmail)
                .anyMatch(userEmail -> userEmail.equals(email));

        if (isExistsEmail) {
            throw new ConflictException(String.format("This \"%s\" email already exists", email));
        }
    }
}