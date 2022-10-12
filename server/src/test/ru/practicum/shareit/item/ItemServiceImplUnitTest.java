package ru.practicum.shareit.item;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.IncorrectUserIdException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplUnitTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @InjectMocks
    private ItemServiceImpl itemService;

    private final Item item = makeItem(1L, "Bicycle", "Very fast bicycle", null,
            true, null);
    private final Item savedItem = makeItem(1L, "Bicycle", "Very fast bicycle", 1L,
            true, null);
    private final User user = makeUser(1L, "test", "user@yandex.ru");
    private final User user2 = makeUser(2L, "test", "user@mail.ru");


    @Test
    void test1_createCorrectItem() {
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemRepository.save(Mockito.any(Item.class)))
                .thenReturn(savedItem);

        ItemDto returnedItem = itemService.createItem(ItemMapper.toItemDto(item), 1L);

        assertThat(returnedItem).isNotNull();
        Mockito.verify(itemRepository, Mockito.times(1)).save(Mockito.any(Item.class));
    }

    @Test
    void test2_createItemIncorrectUserId() {
        assertThrows(UserNotFoundException.class, () -> itemService.createItem(ItemMapper.toItemDto(item), 100L),
                "Incorrect exception");
        Mockito.verify(itemRepository, Mockito.never()).save(Mockito.any(Item.class));
    }

    @Test
    void test3_updateItem() {
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemRepository.getReferenceById(1L))
                .thenReturn(savedItem);
        savedItem.setName("update");
        item.setName("update");
        Mockito
                .when(itemRepository.save(Mockito.any(Item.class)))
                .thenReturn(savedItem);

        ItemDto returnedItem = itemService.updateItem(ItemMapper.toItemDto(item), 1L, 1L);

        assertThat(returnedItem.getId()).isNotNull();
        assertThat(returnedItem).isNotNull();
        Mockito.verify(itemRepository, Mockito.times(1)).save(Mockito.any(Item.class));
    }

    @Test
    void test4_updateItemWhenIsNotOwner() {
        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(user2));

        Mockito
                .when(itemRepository.getReferenceById(1L))
                .thenReturn(savedItem);

        assertThrows(IncorrectUserIdException.class, () -> itemService.updateItem(
                ItemMapper.toItemDto(savedItem), 2L, 1L), "Incorrect exception");
        Mockito.verify(itemRepository, Mockito.never()).save(Mockito.any(Item.class));
    }

    @Test
    void test5_getAllItemsByOwnerWithCorrectPagination() {
        PageRequest pageable = PageRequest.of(0 / 10, 10);
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemRepository.findAllByOwnerId(1L, pageable))
                .thenReturn(List.of(item));

        List<ItemBookingDto> returnedItem = itemService.getAllItemsByOwner(1L, 0, 10);

        AssertionsForClassTypes.assertThat(returnedItem).isNotNull();
        AssertionsForClassTypes.assertThat(returnedItem.size()).isEqualTo(1);
        Mockito.verify(itemRepository, Mockito.times(1)).findAllByOwnerId(1L, pageable);

    }

    @Test
    void test6_findItemById() {
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemRepository.findById(1L))
                .thenReturn(Optional.of(savedItem));

        ItemBookingDto itemDto = itemService.findItemById(1L, 1L);

        assertThat(itemDto).isNotNull();
        assertThat(itemDto.getId()).isEqualTo(1L);
        Mockito.verify(itemRepository, Mockito.times(1)).findById(1L);
    }


    @Test
    void test7_findItemByIncorrectItemId() {
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThrows(ItemNotFoundException.class, () -> itemService.findItemById(100L,
                1L), "Incorrect exception");
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