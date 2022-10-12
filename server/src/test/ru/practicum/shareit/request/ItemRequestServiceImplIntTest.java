package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceImplIntTest {

    private final ItemRequestService itemRequestService;
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Test
    void test1_getAllRequestsWithPagination() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User requester = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        ItemRequest itemRequest = itemRequestRepository.save(makeItemRequest(
                null, "I need some bicycle", requester, LocalDateTime.now(), new ArrayList<>()));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, itemRequest));

        List<ItemRequestWithItemsDto> requests = itemRequestService.getAllRequestsWithPagination(
                owner.getId(), 0, 10);

        Assertions.assertEquals(1, requests.size(), "Incorrect size");
        assertEquals("I need some bicycle", requests.get(0).getDescription(), "Incorrect description");
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
}
