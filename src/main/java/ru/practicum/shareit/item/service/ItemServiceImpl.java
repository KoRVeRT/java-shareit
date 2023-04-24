package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public List<ItemDto> getAllItemsByUserId(Long itemDtoId) {
        log.info("Number of items in the list = {}", itemRepository.getAllItemsByUserId(itemDtoId).size());
        return List.copyOf(itemRepository.getAllItemsByUserId(itemDtoId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList()));
    }

    @Override
    public ItemDto getItemById(Long itemDtoId) {
        log.info("Getting item with id = {}", itemDtoId);
        Item item = itemRepository.getItemById(itemDtoId)
                .orElseThrow(() -> new NotFoundException(String.format("Item with id = %d not found", itemDtoId)));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        User user = UserMapper.toUser(userService.getUserById(userId));
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(user);
        item = itemRepository.createItem(item);
        log.info("Created item with id = {}", item.getId());
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, Long itemId, Long ownerId) {
        Item item = itemRepository.getItemById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Item with id = %d not found", itemId)));
        checkOwner(item, ownerId);
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        item = itemRepository.updateItem(item);
        log.info("Updated item with id = {}", item.getId());
        return ItemMapper.toItemDto(item);
    }

    @Override
    public void deleteItem(Long itemDtoId) {
        getItemById(itemDtoId);
        itemRepository.deleteItem(itemDtoId);
        log.info("Deleted item with id = {}", itemDtoId);
    }

    @Override
    public List<ItemDto> searchItemsByText(String text) {
        log.info("Search item by user request: {}", text);
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return itemRepository.searchItemsByText(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private void checkOwner(Item item, Long userId) {
        if (!Objects.equals(item.getOwner().getId(), userId)) {
            throw new NotFoundException(String.format("Item with id: %d not owned by user with id: %d",
                    item.getId(), userId));
        }
    }
}