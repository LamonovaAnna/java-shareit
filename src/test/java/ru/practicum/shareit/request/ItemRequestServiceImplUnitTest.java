package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.RequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceImplUnitTest {
    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private final User requester = makeUser(2L, "test", "user@yandex.ru");
    private final Item item = makeItem(1L, "Bicycle", "Very fast bicycle", 1L,
            true, null);
    private final ItemRequest request = makeItemRequest(null, "Really great", null,
            LocalDateTime.now(), null);
    private final ItemRequest savedRequest = makeItemRequest(1L, "Really great", requester,
            LocalDateTime.now(), List.of(item));

    @Test
    void test1_createItemRequest() {
        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(requester));
        Mockito
                .when(itemRequestRepository.save(Mockito.any(ItemRequest.class)))
                .thenReturn(savedRequest);

        ItemRequestDto returnedRequest = itemRequestService.createItemRequest(
                ItemRequestMapper.toItemRequestDto(request), 2L);

        assertThat(returnedRequest).isNotNull();
        Mockito.verify(itemRequestRepository, times(1)).save(Mockito.any(ItemRequest.class));
    }

    @Test
    void test2_createItemRequestWithIncorrectUserId() {
        assertThrows(UserNotFoundException.class, () -> itemRequestService.createItemRequest(
                ItemRequestMapper.toItemRequestDto(request), 100L), "Incorrect exception");
        Mockito.verify(itemRequestRepository, Mockito.never()).save(Mockito.any(ItemRequest.class));
    }

    @Test
    void test3_createItemRequestWithIncorrectRequestDescription() {
        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(requester));

        request.setDescription("");
        assertThrows(ValidationException.class, () -> itemRequestService.createItemRequest(
                ItemRequestMapper.toItemRequestDto(request), 2L), "Incorrect exception");
        Mockito.verify(itemRequestRepository, Mockito.never()).save(Mockito.any(ItemRequest.class));
    }

    @Test
    void test4_getAllRequestsByRequester() {
        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(requester));
        Mockito
                .when(itemRequestRepository.findAllByRequesterIdOrderByCreatedAsc(2L))
                .thenReturn(List.of(savedRequest));

        List<ItemRequestWithItemsDto> requests = itemRequestService.getAllRequestsByRequester(2L);

        assertThat(requests.get(0)).isNotNull();
        Mockito.verify(itemRequestRepository, times(1))
                .findAllByRequesterIdOrderByCreatedAsc(Mockito.anyLong());
    }

    @Test
    void test5_getAllRequestsByRequesterWhenEmpty() {
        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(requester));
        Mockito
                .when(itemRequestRepository.findAllByRequesterIdOrderByCreatedAsc(2L))
                .thenReturn(new ArrayList<>());

        List<ItemRequestWithItemsDto> requests = itemRequestService.getAllRequestsByRequester(2L);

        assertEquals(0, requests.size(), "Incorrect list size");
        Mockito.verify(itemRequestRepository, times(1))
                .findAllByRequesterIdOrderByCreatedAsc(Mockito.anyLong());
    }

    @Test
    void test6_getAllRequestsWithPagination() {
        PageRequest pageRequest = PageRequest.of(0 / 10, 10, Sort.by("created").descending());
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(requester));
        Mockito
                .when(itemRequestRepository.findAllByRequesterIdNot(1L, pageRequest))
                .thenReturn(List.of(savedRequest));

        List<ItemRequestWithItemsDto> requests = itemRequestService.getAllRequestsWithPagination(
                1L, 0, 10);

        assertThat(requests.get(0)).isNotNull();
        Mockito.verify(itemRequestRepository, times(1))
                .findAllByRequesterIdNot(1L, pageRequest);
    }

    @Test
    void test7_getRequestById() {
        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(requester));
        Mockito
                .when(itemRequestRepository.findById(1L))
                .thenReturn(Optional.of(savedRequest));

        ItemRequestWithItemsDto returnedRequest = itemRequestService.getRequestById(2L, 1L);

        assertThat(returnedRequest).isNotNull();
        Mockito.verify(itemRequestRepository, times(1)).findById(Mockito.anyLong());
    }

    @MethodSource("test8MethodSource")
    @ParameterizedTest
    void test8_getAllRequestsWithIncorrectPagination(Integer from, Integer size) {
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(requester));

        assertThrows(ValidationException.class, () -> itemRequestService.getAllRequestsWithPagination(1L,
                from, size), "Incorrect exception");
        Mockito.verify(itemRequestRepository, Mockito.never())
                .findAllByRequesterIdNot(1L, Pageable.unpaged());
    }

    private static Stream<Arguments> test8MethodSource() {
        return Stream.of(
                Arguments.of(-1, 10),
                Arguments.of(0, -10)
        );
    }

    @Test
    void test9_getRequestByIncorrectId() {
        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(requester));

        assertThrows(RequestNotFoundException.class, () -> itemRequestService.getRequestById(2L, 100L),
                "Incorrect exception");
    }

    private static ItemRequest makeItemRequest(Long id, String description, User requester, LocalDateTime created,
                                               List<Item> items) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(id);
        itemRequest.setDescription(description);
        itemRequest.setRequester(requester);
        itemRequest.setCreated(created);
        itemRequest.setItems(items);
        return itemRequest;
    }

    private static Item makeItem(Long id, String name, String description, Long ownerId, Boolean isAvailable,
                                 ItemRequest request) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setDescription(description);
        item.setOwnerId(ownerId);
        item.setIsAvailable(isAvailable);
        item.setRequest(request);
        return item;
    }

    private static User makeUser(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}