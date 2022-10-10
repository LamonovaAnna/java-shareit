package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplUnitTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private BookingServiceImpl bookingService;

    private final User owner = makeUser(1L, "test", "user@yandex.ru");
    private final User booker = makeUser(2L, "test", "user@mail.ru");
    private final Item item = makeItem(1L, "Bicycle", "Very fast bicycle", owner.getId(),
            true, null);
    private final Booking booking = makeBooking(null, owner, item, LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2));
    private final Booking savedBooking = makeBooking(1L, booker, item, LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2));

    @Test
    void test1_createBooking() {
        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(booker));

        Mockito
                .when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        Mockito
                .when(bookingRepository.save(Mockito.any(Booking.class)))
                .thenReturn(savedBooking);

        BookingShortDto returnedBooking = bookingService.createBooking(
                BookingMapper.toBookingShortDto(booking), 2L);

        assertThat(returnedBooking).isNotNull();
        Mockito.verify(bookingRepository, Mockito.times(1)).save(Mockito.any(Booking.class));
    }

    @Test
    void test2_createBookingIncorrectItemId() {
        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(booker));

        assertThrows(ItemNotFoundException.class, () -> bookingService.createBooking(
                BookingMapper.toBookingShortDto(booking), 2L), "Incorrect exception");
        Mockito.verify(bookingRepository, Mockito.never()).save(Mockito.any(Booking.class));
    }

    @Test
    void test3_createBookingBookerNotExist() {
        assertThrows(UserNotFoundException.class, () -> bookingService.createBooking(
                BookingMapper.toBookingShortDto(booking), 2L), "Incorrect exception");
        Mockito.verify(bookingRepository, Mockito.never()).save(Mockito.any(Booking.class));
    }

    @Test
    void test4_createBookingIncorrectBookerId() {
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));

        Mockito
                .when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        assertThrows(IncorrectUserIdException.class, () -> bookingService.createBooking(
                BookingMapper.toBookingShortDto(booking), 1L), "Incorrect exception");
        Mockito.verify(bookingRepository, Mockito.never()).save(Mockito.any(Booking.class));
    }

    @Test
    void test5_findBookingById() {
        Mockito
                .when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(savedBooking));

        BookingDto returnedBooking = bookingService.findBookingById(1L, 1L);

        assertThat(returnedBooking).isNotNull();
        Mockito.verify(bookingRepository, Mockito.times(1)).findById(Mockito.anyLong());
    }

    @Test
    void test6_findBookingByIncorrectId() {
        assertThrows(BookingNotFoundException.class, () -> bookingService.findBookingById(100L, 2L),
                "Incorrect exception");
    }

    @Test
    void test7_findBookingByIncorrectUserId() {
        Mockito
                .when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(savedBooking));

        assertThrows(IncorrectUserIdException.class, () -> bookingService.findBookingById(1L, 100L),
                "Incorrect exception");
    }

    @MethodSource("test8MethodSource")
    @ParameterizedTest
    void test8_approveOrRejectBooking(Boolean isApproved, BookingStatus status) {
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(booker));

        Mockito
                .when(bookingRepository.save(Mockito.any(Booking.class)))
                .thenReturn(savedBooking);

        Mockito
                .when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(savedBooking));

        BookingDto returnedBooking = bookingService.approveOrRejectBooking(1L, 1L, isApproved);

        assertThat(returnedBooking).isNotNull();
        assertEquals(returnedBooking.getStatus(), status, "Incorrect booking status");
        Mockito.verify(bookingRepository, Mockito.times(1)).save(Mockito.any(Booking.class));
    }

    private static Stream<Arguments> test8MethodSource() {
        return Stream.of(
                Arguments.of(true, BookingStatus.APPROVED),
                Arguments.of(false, BookingStatus.REJECTED)
        );
    }

    @Test
    void test9_approveOrRejectBookingWhenStatusIsNotWaiting() {
        savedBooking.setStatus(BookingStatus.APPROVED);

        Mockito
                .when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(savedBooking));

        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));

        assertThrows(IncorrectStatusException.class, () -> bookingService.approveOrRejectBooking(
                1L, 1L, true), "Incorrect exception");
        Mockito.verify(bookingRepository, Mockito.never()).save(Mockito.any(Booking.class));
    }

    @Test
    void tes10_createBookingStartWhenItemNotAvailable() {
        item.setIsAvailable(false);

        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(booker));

        Mockito
                .when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));


        assertThrows(ItemNotAvailableException.class, () -> bookingService.createBooking(
                BookingMapper.toBookingShortDto(booking), 2L), "Incorrect exception");
        Mockito.verify(bookingRepository, Mockito.never()).save(Mockito.any(Booking.class));
    }

    @Test
    void test11_approveOrRejectBookingIncorrectOwnerId() {
        Mockito
                .when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(savedBooking));

        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(booker));

        assertThrows(IncorrectUserIdException.class, () -> bookingService.approveOrRejectBooking(
                2L, 1L, true), "Incorrect exception");
        Mockito.verify(bookingRepository, Mockito.never()).save(Mockito.any(Booking.class));
    }

    private static Booking makeBooking(Long id, User user, Item item, LocalDateTime start, LocalDateTime end) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);
        booking.setStartBooking(start);
        booking.setEndBooking(end);
        return booking;
    }

    private static User makeUser(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
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
}