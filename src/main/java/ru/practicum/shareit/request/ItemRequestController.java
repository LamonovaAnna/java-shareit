package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @Autowired
    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @PostMapping()
    public ItemRequestDto createItemRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                     @Valid @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestService.createItemRequest(itemRequestDto, userId);
    }

    @GetMapping
    public List<ItemRequestWithItemsDto> getAllRequestsByRequester(
            @RequestHeader(value = "X-Sharer-User-Id") long userId) {
        return itemRequestService.getAllRequestsByRequester(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestWithItemsDto> getAllRequestsWithPagination(
            @RequestHeader(value = "X-Sharer-User-Id") long userId,
            @RequestParam(required = false) Integer from,
            @RequestParam(required = false) Integer size) {
        return itemRequestService.getAllRequestsWithPagination(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestWithItemsDto getRequestById(@RequestHeader(value = "X-Sharer-User-Id") long userId,
                                                        @PathVariable long requestId) {
        return itemRequestService.getRequestById(userId, requestId);
    }


}
