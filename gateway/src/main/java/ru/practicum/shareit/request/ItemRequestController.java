package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Slf4j
@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private final RequestClient requestClient;
    public static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader(USER_ID_HEADER) @Positive long requesterId,
                                                @RequestBody @Valid ItemRequestDto itemRequestDto) {

        log.info("Creating request {}, userId={}", itemRequestDto, requesterId);
        return requestClient.postRequest(requesterId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getAllOwnRequests(@RequestHeader(USER_ID_HEADER) @Positive long requesterId) {
        log.info("Searching requests of userId={}", requesterId);
        return requestClient.getAllOwnRequests(requesterId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader(USER_ID_HEADER) @Positive long userId,
                                                 @RequestParam(defaultValue = "0")
                                                 @PositiveOrZero(message = "from cannot be negative") int from,
                                                 @RequestParam(defaultValue = "10")
                                                 @Positive(message = "size must be positive") int size) {
        log.info("Get requests of other users, userId={}, from={}, size={}", userId, from, size);
        return requestClient.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequest(@RequestHeader(USER_ID_HEADER) @Positive long userId,
                                             @PathVariable @Positive long requestId) {
        log.info("Get requestId={}, userId={}", requestId, userId);
        return requestClient.getRequest(userId, requestId);
    }
}
