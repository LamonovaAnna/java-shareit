package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createItemRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                                    @RequestBody @Valid ItemRequestDto itemRequestDto) {
        log.info("Creating request {}, userId={}", itemRequestDto, userId);
        return itemRequestClient.createItemRequest(userId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getAllRequestsByRequester(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Get requests by requesterId={}", userId);
        return itemRequestClient.getAllRequestsByRequester(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequestsWithPagination(@RequestHeader("X-Sharer-User-Id") long userId,
                                                               @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                               @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Get requests by userId={}, from={}, size={}", userId, from, size);
        return itemRequestClient.getAllRequestsWithPagination(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findItemById(@RequestHeader(value = "X-Sharer-User-Id") long userId,
                                               @PathVariable long requestId) {
        log.info("Get request {}, userId={}", requestId, userId);
        return itemRequestClient.getRequestById(userId, requestId);
    }
}
