package ru.practicum.shareit.item;

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
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.IncorrectUserIdException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Repository.CommentRepository;
import ru.practicum.shareit.item.Repository.ItemRepository;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;

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

    private final Item item = makeItem(1L,
            "Bicycle",
            "Very fast bicycle",
            null,
            true,
            null);
    private final Item savedItem = makeItem(1L,
            "Bicycle",
            "Very fast bicycle",
            1L,
            true,
            null);

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
        Mockito.verify(itemRepository, times(1)).save(Mockito.any(Item.class));
    }

    @Test
    void test2_createItemIncorrectUserId() {
        assertThrows(UserNotFoundException.class, () -> itemService.createItem(ItemMapper.toItemDto(item), 100L),
                "Incorrect exception");
        Mockito.verify(itemRepository, Mockito.never()).save(Mockito.any(Item.class));
    }

    @MethodSource("test3MethodSource")
    @ParameterizedTest
    void test3_createItemIncorrectParameters(Item item) {
        assertThrows(ValidationException.class, () -> itemService.createItem(ItemMapper.toItemDto(item), 1L),
                "Incorrect exception");
        Mockito.verify(itemRepository, Mockito.never()).save(Mockito.any(Item.class));
    }

    private static Stream<Arguments> test3MethodSource() {
        return Stream.of(
                Arguments.of(makeItem(null, "", "Very fast bicycle", null,
                        true, null)),
                Arguments.of(makeItem(null, null, "Very fast bicycle", null,
                        true, null)),
                Arguments.of(makeItem(null, "Bicycle", "", null,
                        true, null)),
                Arguments.of(makeItem(null, "Bicycle", null, null,
                        true, null)),
                Arguments.of(makeItem(null, "Bicycle", "Very fast bicycle", null,
                        null, null))
        );
    }

    @Test
    void test4_updateItem() {
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
        Mockito.verify(itemRepository, times(1)).save(Mockito.any(Item.class));
    }

    @Test
    void test5_updateItemWhenIsNotOwner() {
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

        assertThat(returnedItem).isNotNull();
        assertThat(returnedItem.size()).isEqualTo(1);
        Mockito.verify(itemRepository, times(1)).findAllByOwnerId(1L, pageable);

    }

    @MethodSource("test6MethodSource")
    @ParameterizedTest
    void test6_getAllItemsByOwnerWithIncorrectPagination(Integer from, Integer size) {
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThrows(ValidationException.class, () -> itemService.getAllItemsByOwner(1L,
                from, size), "Incorrect exception");
        Mockito.verify(itemRepository, Mockito.never()).findAllByOwnerId(1L, Pageable.unpaged());
    }

    private static Stream<Arguments> test6MethodSource() {
        return Stream.of(
                Arguments.of(-1, 10),
                Arguments.of(0, -10)
        );
    }

    @Test
    void test7_findItemById() {
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemRepository.findById(1L))
                .thenReturn(Optional.of(savedItem));

        ItemBookingDto itemDto = itemService.findItemById(1L, 1L);

        assertThat(itemDto).isNotNull();
        assertThat(itemDto.getId()).isEqualTo(1L);
        Mockito.verify(itemRepository, times(1)).findById(1L);
    }


    @Test
    void test8_findItemByIncorrectItemId() {
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
