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
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
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
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User booker = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));
        bookingRepository.save(makeBooking(null, booker, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)));
        bookingRepository.save(makeBooking(null, booker, item, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)));

        List<ItemBookingDto> items = itemService.getAllItemsByOwner(owner.getId(), 0, 10);

        Assertions.assertNotNull(items);
        Assertions.assertEquals(1, items.size(), "Incorrect list size");
        assertNotNull(items.get(0).getLastBooking(), "Incorrect last booking");
        assertNotNull(items.get(0).getNextBooking(), "Incorrect next booking");
    }

    @Test
    void test2_findItemById() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User booker = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));
        bookingRepository.save(makeBooking(null, booker, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)));
        bookingRepository.save(makeBooking(null, booker, item, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)));
        Comment comment = commentRepository.save(makeComment(null, booker, item, "Really great"));

        ItemBookingDto returnedItem = itemService.findItemById(item.getId(), owner.getId());

        Assertions.assertNotNull(returnedItem);
        assertEquals(item.getId(), returnedItem.getId(), "Incorrect Id");
        assertEquals("Bicycle", returnedItem.getName(), "Incorrect name");
        Assertions.assertNotNull(returnedItem.getLastBooking());
        Assertions.assertNotNull(returnedItem.getComments());
        assertEquals(comment.getId(), returnedItem.getComments().stream().findFirst().get().getId(),
                "Incorrect comment id");
    }

    @Test
    void test3_findItemByIdUserIsNotOwner() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User booker = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));
        bookingRepository.save(makeBooking(null, booker, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)));
        bookingRepository.save(makeBooking(null, booker, item, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)));
        commentRepository.save(makeComment(null, booker, item, "Really great"));

        ItemBookingDto returnedItem = itemService.findItemById(item.getId(), booker.getId());

        Assertions.assertNotNull(returnedItem);
        assertEquals(item.getId(), returnedItem.getId(), "Incorrect Id");
        assertEquals("Bicycle", returnedItem.getName(), "Incorrect name");
        Assertions.assertNull(returnedItem.getLastBooking());
        Assertions.assertNotNull(returnedItem.getComments());
        assertEquals(1L, returnedItem.getComments().stream().findFirst().get().getId(),
                "Incorrect comment id");
    }

    @Test
    void test3_deleteItem() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));

        itemService.deleteItem(item.getId(), owner.getId());

        assertFalse(itemRepository.existsById(item.getId()), "Item wasn't deleted");
    }

    @Test
    void test4_deleteItemWithIncorrectOwnerId() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User booker = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));

        final IncorrectUserIdException exception = Assertions.assertThrows(IncorrectUserIdException.class,
                () -> itemService.deleteItem(item.getId(), booker.getId()));
        assertEquals("Access error", exception.getMessage(),
                "incorrect message");

        assertTrue(itemRepository.existsById(item.getId()), "Item was deleted");
    }

    @Test
    void test5_findItemsByName() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));
        itemRepository.save(makeItem(null, "Book", "Very interesting book",
                owner.getId(), true, null));

        List<ItemDto> foundBicycle = itemService.findItemsByNameOrDescription("bicy", 0, 10);

        Assertions.assertNotNull(foundBicycle);
        Assertions.assertEquals(1, foundBicycle.size(), "Incorrect list size");
        assertEquals("Bicycle", foundBicycle.get(0).getName(), "Was found incorrect item");
    }

    @Test
    void test6_findItemsByDescription() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));
        itemRepository.save(makeItem(null, "Book", "Very interesting book",
                owner.getId(), true, null));

        List<ItemDto> foundNothing = itemService.findItemsByNameOrDescription("driv", 0, 10);
        Assertions.assertEquals(0, foundNothing.size(), "Incorrect list size");
    }

    @Test
    void test7_findItemsByNameOrDescriptionWhenTheRequestedItemIsNotFound() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));
        itemRepository.save(makeItem(null, "Book", "Very interesting book",
                owner.getId(), true, null));

        List<ItemDto> foundBook = itemService.findItemsByNameOrDescription("inter", 0, 10);
        Assertions.assertNotNull(foundBook);
        Assertions.assertEquals(1, foundBook.size(), "Incorrect list size");
        assertEquals("Book", foundBook.get(0).getName(), "Was found incorrect item");
    }

    @Test
    void test8_createCommentToItem() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User booker = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));
        bookingRepository.save(makeBooking(null, booker, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)));

        CommentDto comment = itemService.createCommentToItem(booker.getId(),
                CommentMapper.toCommentDto(makeComment(null, booker, item, "Really great")),
                item.getId());

        Assertions.assertNotNull(comment);
        assertEquals("Really great", comment.getText(), "Incorrect text");
        assertNotNull(comment.getItem(), "Item is missing");
    }

    @Test
    void test9_createCommentToItemWithIncorrectAuthor() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User booker = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));
        bookingRepository.save(makeBooking(null, booker, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)));

        final UserNotFoundException exception = Assertions.assertThrows(UserNotFoundException.class,
                () -> itemService.createCommentToItem(3L,
                        CommentMapper.toCommentDto(makeComment(null, booker, item, "Really great")),
                        item.getId()));
        assertEquals("This user doesn't exist", exception.getMessage(),
                "incorrect message");
        assertThrows(UserNotFoundException.class, () -> itemService.createCommentToItem(3L,
                CommentMapper.toCommentDto(makeComment(null, booker, item, "Really great")),
                item.getId()), "Incorrect exception");
    }

    @Test
    void test10_createCommentToItemWithIncorrectCommentText() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User booker = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));
        bookingRepository.save(makeBooking(null, booker, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)));

        final ValidationException exception = Assertions.assertThrows(ValidationException.class,
                () -> itemService.createCommentToItem(booker.getId(),
                        CommentMapper.toCommentDto(makeComment(null, booker, item, "")),
                        item.getId()));
        assertEquals("Comment can't be empty", exception.getMessage(),
                "incorrect message");
        assertThrows(ValidationException.class, () -> itemService.createCommentToItem(booker.getId(),
                CommentMapper.toCommentDto(makeComment(null, booker, item, "")),
                item.getId()), "Incorrect exception");
    }

    @Test
    void tes11_createCommentToItemWithIncorrectItemId() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User booker = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));
        bookingRepository.save(makeBooking(null, booker, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)));

        assertThrows(IncorrectUserIdException.class, () -> itemService.createCommentToItem(booker.getId(),
                CommentMapper.toCommentDto(makeComment(null, booker, item, "Really great")),
                3L), "Incorrect exception");
    }

    @Test
    void test12_createCommentToItemWhenBookingNotFound() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User booker = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));

        final BookingNotFoundException exception = Assertions.assertThrows(BookingNotFoundException.class,
                () -> itemService.createCommentToItem(booker.getId(),
                        CommentMapper.toCommentDto(makeComment(null, booker, item, "Really great")),
                        item.getId()));
        assertEquals("This booking doesn't exist", exception.getMessage(),
                "incorrect message");
        assertThrows(BookingNotFoundException.class, () -> itemService.createCommentToItem(booker.getId(),
                CommentMapper.toCommentDto(makeComment(null, booker, item, "Really great")),
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
