package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.RequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
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
        validateRequest(itemRequestDto);
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
            checkPagination(from, size);
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

    private void validateRequest(ItemRequestDto itemRequestDto) {
        if (itemRequestDto.getDescription() == null || itemRequestDto.getDescription().isBlank()) {
            log.info("Field \"description\" doesn't filled");
            throw new ValidationException("Incorrect request's description");
        }
    }

    private void checkPagination(Integer from, Integer size) {
        if (from < 0) {
            log.info("Parameter \"from\" have to be above or equals zero");
            throw new ValidationException("Incorrect parameter \"from\"");
        }
        if (size <= 0) {
            log.info("Parameter \"size\" have to be above zero");
            throw new ValidationException("Incorrect parameter \"size\"");
        }
    }
}