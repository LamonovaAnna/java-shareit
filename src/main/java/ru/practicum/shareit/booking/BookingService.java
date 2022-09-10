package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {

    BookingShortDto createBooking(BookingShortDto bookingDto, long bookerId);

    BookingDto findBookingById(long bookingId, long userId);

    List<BookingDto> findBookingsByBooker(long bookerId, String state);

    List<BookingDto> findBookingsByOwner(long ownerId, String state);

    BookingDto approveOrRejectBooking(long ownerId, long bookingId, boolean isApproved);

}
