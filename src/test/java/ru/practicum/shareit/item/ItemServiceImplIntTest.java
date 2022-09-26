package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.IncorrectUserIdException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Repository.CommentRepository;
import ru.practicum.shareit.item.Repository.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImplIntTest {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemService itemService;

    @Test
    void test1_getAllItemsByOwner() {
        User user1 = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User user2 = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                user1.getId(), true, null));
        bookingRepository.save(makeBooking(null, user2, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)));
        bookingRepository.save(makeBooking(null, user2, item, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)));


        List<ItemBookingDto> items = itemService.getAllItemsByOwner(user1.getId(), 0, 10);

        assertNotNull(items);
        assertEquals(items.size(), 1, "Incorrect list size");
        assertNotNull(items.get(0).getLastBooking(), "Incorrect last booking");
        assertNotNull(items.get(0).getNextBooking(), "Incorrect next booking");
    }

    @Test
    void test2_findItemById() {
        User user1 = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User user2 = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                user1.getId(), true, null));
        bookingRepository.save(makeBooking(null, user2, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)));
        bookingRepository.save(makeBooking(null, user2, item, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)));
        Comment comment = commentRepository.save(makeComment(null, user2, item, "Really great"));

        ItemBookingDto returnedItem = itemService.findItemById(item.getId(), user1.getId());

        assertNotNull(returnedItem);
        assertEquals(returnedItem.getId(), item.getId(), "Incorrect Id");
        assertEquals(returnedItem.getName(), "Bicycle", "Incorrect name");
        assertNotNull(returnedItem.getLastBooking());
        assertNotNull(returnedItem.getComments());
        assertEquals(returnedItem.getComments().stream().findFirst().get().getId(), comment.getId(),
                "Incorrect comment id");
    }

    @Test
    void test3_findItemByIdUserIsNotOwner() {
        User user1 = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User user2 = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                user1.getId(), true, null));
        bookingRepository.save(makeBooking(null, user2, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)));
        bookingRepository.save(makeBooking(null, user2, item, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)));
        commentRepository.save(makeComment(null, user2, item, "Really great"));

        ItemBookingDto returnedItem = itemService.findItemById(item.getId(), user2.getId());

        assertNotNull(returnedItem);
        assertEquals(returnedItem.getId(), item.getId(), "Incorrect Id");
        assertEquals(returnedItem.getName(), "Bicycle", "Incorrect name");
        assertNull(returnedItem.getLastBooking());
        assertNotNull(returnedItem.getComments());
        assertEquals(returnedItem.getComments().stream().findFirst().get().getId(), 1L,
                "Incorrect comment id");
    }

    @Test
    void test3_deleteItem() {
        User user1 = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                user1.getId(), true, null));

        itemService.deleteItem(item.getId(), user1.getId());

        assertFalse(itemRepository.existsById(item.getId()), "Item wasn't deleted");
    }

    @Test
    void test4_deleteItemWithIncorrectOwnerId() {
        User user1 = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User user2 = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                user1.getId(), true, null));

        final IncorrectUserIdException exception = assertThrows(IncorrectUserIdException.class,
                () -> itemService.deleteItem(item.getId(), user2.getId()));
        assertEquals("Access error", exception.getMessage(),
                "incorrect message");
        assertThrows(IncorrectUserIdException.class, () -> itemService.deleteItem(item.getId(), user2.getId()),
                "Incorrect exception");

        assertTrue(itemRepository.existsById(item.getId()), "Item was deleted");
    }

    @Test
    void test5_findItemsByNameOrDescription() {
        User user1 = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                user1.getId(), true, null));
        itemRepository.save(makeItem(null, "Book", "Very interesting book",
                user1.getId(), true, null));

        List<ItemDto> foundBicycle = itemService.findItemsByNameOrDescription("bicy", 0, 10);

        assertNotNull(foundBicycle);
        assertEquals(foundBicycle.size(), 1, "Incorrect list size");
        assertEquals(foundBicycle.get(0).getName(), "Bicycle", "Was found incorrect item");

        List<ItemDto> foundBook = itemService.findItemsByNameOrDescription("inter", 0, 10);
        assertNotNull(foundBook);
        assertEquals(foundBook.size(), 1, "Incorrect list size");
        assertEquals(foundBook.get(0).getName(), "Book", "Was found incorrect item");

        List<ItemDto> foundNothing = itemService.findItemsByNameOrDescription("driv", 0, 10);
        assertEquals(foundNothing.size(), 0, "Incorrect list size");
    }

    @Test
    void test6_createCommentToItem() {
        User user1 = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User user2 = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                user1.getId(), true, null));
        bookingRepository.save(makeBooking(null, user2, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)));

        CommentDto comment = itemService.createCommentToItem(user2.getId(),
                CommentMapper.toCommentDto(makeComment(null, user2, item, "Really great")),
                item.getId());

        assertNotNull(comment);
        assertEquals(comment.getText(), "Really great", "Incorrect text");
    }

    @Test
    void test7_createCommentToItemWithIncorrectAuthor() {
        User user1 = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User user2 = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                user1.getId(), true, null));
        bookingRepository.save(makeBooking(null, user2, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)));

        final UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> itemService.createCommentToItem(3L,
                        CommentMapper.toCommentDto(makeComment(null, user2, item, "Really great")),
                        item.getId()));
        assertEquals("This user doesn't exist", exception.getMessage(),
                "incorrect message");
        assertThrows(UserNotFoundException.class, () -> itemService.createCommentToItem(3L,
                CommentMapper.toCommentDto(makeComment(null, user2, item, "Really great")),
                item.getId()), "Incorrect exception");
    }

    @Test
    void test8_createCommentToItemWithIncorrectCommentText() {
        User user1 = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User user2 = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                user1.getId(), true, null));
        bookingRepository.save(makeBooking(null, user2, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)));

        final ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.createCommentToItem(user2.getId(),
                        CommentMapper.toCommentDto(makeComment(null, user2, item, "")),
                        item.getId()));
        assertEquals("Comment can't be empty", exception.getMessage(),
                "incorrect message");
        assertThrows(ValidationException.class, () -> itemService.createCommentToItem(user2.getId(),
                CommentMapper.toCommentDto(makeComment(null, user2, item, "")),
                item.getId()), "Incorrect exception");
    }

    @Test
    void test9_createCommentToItemWithIncorrectItemId() {
        User user1 = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User user2 = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                user1.getId(), true, null));
        bookingRepository.save(makeBooking(null, user2, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)));

        assertThrows(IncorrectUserIdException.class, () -> itemService.createCommentToItem(user2.getId(),
                CommentMapper.toCommentDto(makeComment(null, user2, item, "Really great")),
                3L), "Incorrect exception");
    }

    @Test
    void test10_createCommentToItemWhenBookingNotFound() {
        User user1 = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User user2 = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                user1.getId(), true, null));

        final BookingNotFoundException exception = assertThrows(BookingNotFoundException.class,
                () -> itemService.createCommentToItem(user2.getId(),
                        CommentMapper.toCommentDto(makeComment(null, user2, item, "Really great")),
                        item.getId()));
        assertEquals("This booking doesn't exist", exception.getMessage(),
                "incorrect message");
        assertThrows(BookingNotFoundException.class, () -> itemService.createCommentToItem(user2.getId(),
                CommentMapper.toCommentDto(makeComment(null, user2, item, "Really great")),
                item.getId()), "Incorrect exception");
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

    private static Booking makeBooking(Long id, User user, Item item, LocalDateTime start, LocalDateTime end) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStartBooking(start);
        booking.setEndBooking(end);
        return booking;
    }

    private static Comment makeComment(Long id, User user, Item item, String text) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setText(text);
        comment.setCreated(LocalDateTime.now());
        return comment;
    }
}
