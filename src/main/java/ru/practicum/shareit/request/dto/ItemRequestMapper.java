package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto, Long requesterId) {
        if (itemRequestDto == null) {
            return null;
        }

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(itemRequestDto.getId() != null ? itemRequestDto.getId() : null);
        itemRequest.setDescription(itemRequestDto.getDescription());
        itemRequest.setRequester(UserMapper.toUser(new UserDto(requesterId, null, null)));
        itemRequest.setCreated(LocalDateTime.now());
        return itemRequest;
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        if (itemRequest == null) {
            return null;
        }
        return new ItemRequestDto(itemRequest.getId(),
                itemRequest.getDescription(),
                new UserShortDto(itemRequest.getRequester().getId(), itemRequest.getRequester().getName()),
                itemRequest.getCreated());
    }

    public static ItemRequestWithItemsDto toItemRequestWithItemsDto(ItemRequest itemRequest) {
        if (itemRequest == null) {
            return null;
        }
        return new ItemRequestWithItemsDto(itemRequest.getId(),
                itemRequest.getDescription(),
                new UserShortDto(itemRequest.getRequester().getId(), itemRequest.getRequester().getName()),
                itemRequest.getCreated(),
                ItemMapper.toItemsDto(new ArrayList<>(itemRequest.getItems())));
    }

    public static List<ItemRequestWithItemsDto> toItemsRequestsDto(List<ItemRequest> requests) {
        return requests.stream().map(ItemRequestMapper::toItemRequestWithItemsDto).collect(Collectors.toList());
    }
}
