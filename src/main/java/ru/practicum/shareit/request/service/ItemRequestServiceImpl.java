package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;
    private static final String ITEM_REQUEST_REQUESTOR_USER_FIELD_NAME = "requestor";
    private static final String ITEM_REQUEST_CREATED_DATE_FIELD_NAME = "created";


    @Override
    @Transactional
    public ItemRequestDto createRequest(ItemRequestDto itemRequestDto) {
        User user = findUserById(itemRequestDto.getRequestorId());
        ItemRequest itemRequest = itemRequestMapper.toItemRequest(itemRequestDto);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setRequestor(user);
        itemRequest = itemRequestRepository.save(itemRequest);
        log.info("Created request with id:{}", itemRequest.getId());
        return itemRequestMapper.toItemRequestDto(itemRequest);
    }

    @Override
    public List<ItemRequestDto> getOwnRequestsByUserId(long userId) {
        findUserById(userId);
        Specification<ItemRequest> byUserId = (r, q, cb) -> cb.equal(
                r.<User>get(ITEM_REQUEST_REQUESTOR_USER_FIELD_NAME).get("id"), userId
        );
        return itemRequestRepository.findAll(byUserId, Sort.by(Sort.Direction.DESC,
                        ITEM_REQUEST_CREATED_DATE_FIELD_NAME)).stream()
                .map(x -> {
                    ItemRequestDto itemRequestDto = itemRequestMapper.toItemRequestDto(x);
                    itemRequestDto.setItems(addItems(x.getId()));
                    return itemRequestDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(int from, int size, long userId) {
        findUserById(userId);
        Specification<ItemRequest> byUserId = (r, q, cb) -> cb.notEqual(
                r.<User>get(ITEM_REQUEST_REQUESTOR_USER_FIELD_NAME).get("id"), userId
        );
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(
                Sort.Direction.DESC, ITEM_REQUEST_CREATED_DATE_FIELD_NAME
        ));
        return itemRequestRepository.findAll(byUserId, pageable).stream()
                .map(x -> {
                    ItemRequestDto itemRequestDto = itemRequestMapper.toItemRequestDto(x);
                    itemRequestDto.setItems(addItems(x.getId()));
                    return itemRequestDto;
                })
                .collect(Collectors.toList());
    }


    @Override
    public ItemRequestDto getRequestById(long requestId, long userId) {
        findUserById(userId);
        ItemRequestDto itemRequestDto = itemRequestMapper.toItemRequestDto(findRequestById(requestId));
        itemRequestDto.setItems(addItems(requestId));
        log.info("Getting request with id:{}", requestId);
        return itemRequestDto;
    }

    private User findUserById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id:%d not found", userId)));
    }

    private ItemRequest findRequestById(long requestId) {
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Request with id:%d not found", requestId)));
    }

    private List<ItemDto> addItems(long requestId) {
        return itemRepository.findAllByRequestId(requestId).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}