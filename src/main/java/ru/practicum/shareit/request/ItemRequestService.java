package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createItemRequest(ItemRequestDto itemRequestDto, Long userId);

    List<ItemRequestWithItemsDto> getAllRequestsByRequester(long userId);

    List<ItemRequestWithItemsDto> getAllRequestsWithPagination(long userId, Integer from, Integer size);

    ItemRequestWithItemsDto getRequestById(long userId, long requestId);
}
