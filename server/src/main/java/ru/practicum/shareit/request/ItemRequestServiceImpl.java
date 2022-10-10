package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.RequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.user.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    @Override
    public ItemRequestDto createItemRequest(ItemRequestDto itemRequestDto, Long userId) {
        checkUserExist(userId);
        return ItemRequestMapper.toItemRequestDto(itemRequestRepository.save(
                ItemRequestMapper.toItemRequest(itemRequestDto, userId)));
    }

    @Override
    public List<ItemRequestWithItemsDto> getAllRequestsByRequester(long userId) {
        checkUserExist(userId);
        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterIdOrderByCreatedAsc(userId);
        if (!requests.isEmpty()) {
            return ItemRequestMapper.toItemsRequestsDto(requests);
        }
        return new ArrayList<>();
    }

    @Override
    public List<ItemRequestWithItemsDto> getAllRequestsWithPagination(long userId, Integer from, Integer size) {
        checkUserExist(userId);

        List<ItemRequestWithItemsDto> requests = new ArrayList<>();
        if (from != null && size != null) {
            List<ItemRequest> requestTemp = itemRequestRepository.findAllByRequesterIdNot(
                    userId, PageRequest.of(from / size, size, Sort.by("created").descending()));
            if (!requestTemp.isEmpty()) {
                requests.addAll(ItemRequestMapper.toItemsRequestsDto(requestTemp));
            }
        }
        return requests;
    }

    @Override
    public ItemRequestWithItemsDto getRequestById(long userId, long requestId) {
        checkUserExist(userId);
        return ItemRequestMapper.toItemRequestWithItemsDto(itemRequestRepository.findById(requestId)
                .orElseThrow(RequestNotFoundException::new));
    }

    private void checkUserExist(long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.info("User with id {} not found", userId);
            throw new UserNotFoundException();
        }
    }
}