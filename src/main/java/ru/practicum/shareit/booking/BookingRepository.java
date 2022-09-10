package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBookerId(long bookerId);

    List<Booking> findAllByBookerIdAndStartBookingIsBeforeAndEndBookingIsAfter(
            long bookerId, LocalDateTime startTime, LocalDateTime endTime);

    List<Booking> findAllByBookerIdAndEndBookingIsBefore(long bookerId, LocalDateTime time);

    List<Booking> findAllByBookerIdAndStartBookingIsAfter(long bookerId, LocalDateTime time);

    List<Booking> findAllByBookerIdAndStatus(long bookerId, BookingStatus status);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "WHERE b.startBooking < CURRENT_TIMESTAMP " +
            "AND b.endBooking > CURRENT_TIMESTAMP " +
            "AND b.item.ownerId = ?1")
    List<Booking> findAllCurrentBookingsByOwner(long ownerId);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "WHERE b.endBooking < CURRENT_TIMESTAMP " +
            "AND b.item.ownerId = ?1")
    List<Booking> findAllPastBookingsByOwner(long ownerId);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "WHERE b.startBooking > CURRENT_TIMESTAMP " +
            "AND b.item.ownerId = ?1")
    List<Booking> findAllFutureBookingsByOwner(long ownerId);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "WHERE b.status = ?2 " +
            "AND b.item.ownerId = ?1")
    List<Booking> findAllBookingsByOwnerAndStatus(long ownerId, BookingStatus status);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "WHERE b.item.ownerId = ?1")
    List<Booking> findAllBookingsByOwner(long ownerId);

    List<Booking> findBookingByItemIdAndEndBookingIsBeforeOrderByEndBookingDesc(long itemId, LocalDateTime time);

    List<Booking> findBookingByItemIdAndStartBookingIsAfterOrderByStartBookingAsc(long itemId, LocalDateTime time);
}
