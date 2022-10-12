package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.exception.IncorrectStatusException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
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
public class BookingServiceImplIntTest {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingService bookingService;

    @Test
    void test1_findBookingsByBookerWhenStateIsCurrent() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User booker = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));
        bookingRepository.save(makeBooking(booker, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().plusDays(1)));
        bookingRepository.save(makeBooking(booker, item, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5)));

        List<BookingDto> bookings = bookingService.findBookingsByBooker(
                booker.getId(), "CURRENT", 0, 10);

        Assertions.assertEquals(1, bookings.size(), "Incorrect size");
        assertEquals(booker.getId(), bookings.get(0).getBooker().getId(), "Incorrect booker id");
        assertTrue(bookings.get(0).getStart().isBefore(LocalDateTime.now()), "Incorrect start time");
        assertTrue(bookings.get(0).getEnd().isAfter(LocalDateTime.now()), "Incorrect start time");
    }

    @Test
    void test2_findBookingsByBookerWhenStateIsPast() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User booker = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));
        bookingRepository.save(makeBooking(booker, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)));
        bookingRepository.save(makeBooking(booker, item, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5)));

        List<BookingDto> bookings = bookingService.findBookingsByBooker(
                booker.getId(), "PAST", 0, 10);

        Assertions.assertEquals(1, bookings.size(), "Incorrect size");
        assertEquals(booker.getId(), bookings.get(0).getBooker().getId(), "Incorrect booker id");
        assertTrue(bookings.get(0).getStart().isBefore(LocalDateTime.now()), "Incorrect start time");
        assertTrue(bookings.get(0).getEnd().isBefore(LocalDateTime.now()), "Incorrect start time");
    }

    @Test
    void test3_findBookingsByBookerWhenStateIsFuture() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User booker = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));
        bookingRepository.save(makeBooking(booker, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)));
        bookingRepository.save(makeBooking(booker, item, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5)));

        List<BookingDto> bookings = bookingService.findBookingsByBooker(
                booker.getId(), "FUTURE", 0, 10);

        Assertions.assertEquals(1, bookings.size(), "Incorrect size");
        assertEquals(booker.getId(), bookings.get(0).getBooker().getId(), "Incorrect booker id");
        assertTrue(bookings.get(0).getStart().isAfter(LocalDateTime.now()), "Incorrect start time");
        assertTrue(bookings.get(0).getEnd().isAfter(LocalDateTime.now()), "Incorrect start time");
    }

    @Test
    void test4_findBookingsByBookerWhenStateIsWaiting() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User booker = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));
        bookingRepository.save(makeBooking(booker, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)));
        Booking booking = makeBooking(booker, item, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5));
        booking.setStatus(BookingStatus.WAITING);
        bookingRepository.save(booking);

        List<BookingDto> bookings = bookingService.findBookingsByBooker(
                booker.getId(), "WAITING", 0, 10);

        Assertions.assertEquals(1, bookings.size(), "Incorrect size");
        assertEquals(booker.getId(), bookings.get(0).getBooker().getId(), "Incorrect booker id");
        assertEquals(BookingStatus.WAITING, bookings.get(0).getStatus(), "Incorrect status");
    }

    @Test
    void test5_findBookingsByBookerWhenStateIsRejected() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User booker = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));
        bookingRepository.save(makeBooking(booker, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)));
        Booking booking = makeBooking(booker, item, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5));
        booking.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(booking);

        List<BookingDto> bookings = bookingService.findBookingsByBooker(
                booker.getId(), "REJECTED", 0, 10);

        Assertions.assertEquals(1, bookings.size(), "Incorrect size");
        assertEquals(booker.getId(), bookings.get(0).getBooker().getId(), "Incorrect booker id");
        assertEquals(BookingStatus.REJECTED, bookings.get(0).getStatus(), "Incorrect status");
    }

    @Test
    void test6_findBookingsByBookerWhenStateIsAll() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User booker = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));
        Booking pastBooking = bookingRepository.save(makeBooking(booker, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)));
        Booking nextBooking = bookingRepository.save(makeBooking(booker, item, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5)));

        List<BookingDto> bookings = bookingService.findBookingsByBooker(
                booker.getId(), "ALL", 0, 10);

        Assertions.assertEquals(2, bookings.size(), "Incorrect size");
        assertEquals(BookingMapper.toBookingDto(pastBooking), bookings.get(1), "Incorrect sorting");
        assertEquals(BookingMapper.toBookingDto(nextBooking), bookings.get(0), "Incorrect sorting");
    }

    @Test
    void test7_findBookingsByBookerWhenStateIsIncorrect() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User booker = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));
        bookingRepository.save(makeBooking(booker, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)));
        bookingRepository.save(makeBooking(booker, item, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5)));

        final IncorrectStatusException exception = Assertions.assertThrows(IncorrectStatusException.class,
                () -> bookingService.findBookingsByBooker(booker.getId(), "APPROVED", 0, 10));
        assertEquals("Unknown state: UNSUPPORTED_STATUS", exception.getMessage(),
                "Incorrect message");
        assertThrows(IncorrectStatusException.class, () -> bookingService.findBookingsByBooker(
                booker.getId(), "APPROVED", 0, 10), "Incorrect exception");
    }

    @Test
    void test8_findBookingsByOwnerWhenStateIsCurrent() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User booker = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));
        bookingRepository.save(makeBooking(booker, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().plusDays(1)));
        bookingRepository.save(makeBooking(booker, item, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5)));

        List<BookingDto> bookings = bookingService.findBookingsByOwner(
                owner.getId(), "CURRENT", 0, 10);

        Assertions.assertEquals(1, bookings.size(), "Incorrect size");
        assertEquals(item.getId(), bookings.get(0).getItem().getId(), "Incorrect item id");
        assertTrue(bookings.get(0).getStart().isBefore(LocalDateTime.now()), "Incorrect start time");
        assertTrue(bookings.get(0).getEnd().isAfter(LocalDateTime.now()), "Incorrect start time");
    }

    @Test
    void test9_findBookingsByOwnerWhenStateIsPast() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User booker = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));
        bookingRepository.save(makeBooking(booker, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)));
        bookingRepository.save(makeBooking(booker, item, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5)));

        List<BookingDto> bookings = bookingService.findBookingsByOwner(
                owner.getId(), "PAST", 0, 10);

        Assertions.assertEquals(1, bookings.size(), "Incorrect size");
        assertEquals(item.getId(), bookings.get(0).getItem().getId(), "Incorrect item id");
        assertTrue(bookings.get(0).getStart().isBefore(LocalDateTime.now()), "Incorrect start time");
        assertTrue(bookings.get(0).getEnd().isBefore(LocalDateTime.now()), "Incorrect start time");
    }

    @Test
    void test10_findBookingsByOwnerWhenStateIsFuture() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User booker = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));
        bookingRepository.save(makeBooking(booker, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)));
        bookingRepository.save(makeBooking(booker, item, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5)));

        List<BookingDto> bookings = bookingService.findBookingsByOwner(
                owner.getId(), "FUTURE", 0, 10);

        Assertions.assertEquals(1, bookings.size(), "Incorrect size");
        assertEquals(item.getId(), bookings.get(0).getItem().getId(), "Incorrect item id");
        assertTrue(bookings.get(0).getStart().isAfter(LocalDateTime.now()), "Incorrect start time");
        assertTrue(bookings.get(0).getEnd().isAfter(LocalDateTime.now()), "Incorrect start time");
    }

    @Test
    void test11_findBookingsByOwnerWhenStateIsWaiting() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User booker = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));
        bookingRepository.save(makeBooking(booker, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)));
        Booking booking = makeBooking(booker, item, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5));
        booking.setStatus(BookingStatus.WAITING);
        bookingRepository.save(booking);

        List<BookingDto> bookings = bookingService.findBookingsByOwner(
                owner.getId(), "WAITING", 0, 10);

        Assertions.assertEquals(1, bookings.size(), "Incorrect size");
        assertEquals(item.getId(), bookings.get(0).getItem().getId(), "Incorrect item id");
        assertEquals(BookingStatus.WAITING, bookings.get(0).getStatus(), "Incorrect status");
    }

    @Test
    void test12_findBookingsByOwnerWhenStateIsRejected() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User booker = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));
        bookingRepository.save(makeBooking(booker, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)));
        Booking booking = makeBooking(booker, item, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5));
        booking.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(booking);

        List<BookingDto> bookings = bookingService.findBookingsByOwner(
                owner.getId(), "REJECTED", 0, 10);

        Assertions.assertEquals(1, bookings.size(), "Incorrect size");
        assertEquals(item.getId(), bookings.get(0).getItem().getId(), "Incorrect item id");
        assertEquals(BookingStatus.REJECTED, bookings.get(0).getStatus(), "Incorrect status");
    }

    @Test
    void test13_findBookingsByOwnerWhenStateIsAll() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User booker = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));
        Booking pastBooking = bookingRepository.save(makeBooking(booker, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)));
        Booking nextBooking = bookingRepository.save(makeBooking(booker, item, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5)));

        List<BookingDto> bookings = bookingService.findBookingsByOwner(
                owner.getId(), "ALL", 0, 10);

        Assertions.assertEquals(2, bookings.size(), "Incorrect size");
        assertEquals(BookingMapper.toBookingDto(pastBooking), bookings.get(1), "Incorrect sorting");
        assertEquals(BookingMapper.toBookingDto(nextBooking), bookings.get(0), "Incorrect sorting");
    }

    @Test
    void test14_findBookingsByOwnerWhenStateIsIncorrect() {
        User owner = userRepository.save(makeUser(null, "test", "test@mail.ru"));
        User booker = userRepository.save(makeUser(null, "test", "test@yandex.ru"));
        Item item = itemRepository.save(makeItem(null, "Bicycle", "Very fast bicycle",
                owner.getId(), true, null));
        bookingRepository.save(makeBooking(booker, item, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)));
        bookingRepository.save(makeBooking(booker, item, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5)));

        final IncorrectStatusException exception = Assertions.assertThrows(IncorrectStatusException.class,
                () -> bookingService.findBookingsByOwner(owner.getId(), "APPROVED", 0, 10));
        assertEquals("Unknown state: UNSUPPORTED_STATUS", exception.getMessage(),
                "Incorrect message");
        assertThrows(IncorrectStatusException.class, () -> bookingService.findBookingsByOwner(
                owner.getId(), "APPROVED", 0, 10), "Incorrect exception");
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

    private static Booking makeBooking(User user, Item item, LocalDateTime start, LocalDateTime end) {
        Booking booking = new Booking();
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStartBooking(start);
        booking.setEndBooking(end);
        return booking;
    }
}