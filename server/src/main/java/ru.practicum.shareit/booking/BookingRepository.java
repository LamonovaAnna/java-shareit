package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBookerId(long bookerId, Pageable pageable);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "WHERE b.booker.id = ?1 " +
            "AND b.startBooking < CURRENT_TIMESTAMP " +
            "AND b.endBooking > CURRENT_TIMESTAMP ")
    List<Booking> findAllCurrentBookingsByBookerId(long bookerId, Pageable pageable);

    List<Booking> findAllByBookerIdAndEndBookingIsBefore(long bookerId, LocalDateTime time, Pageable pageable);

    List<Booking> findAllByBookerIdAndStartBookingIsAfter(long bookerId, LocalDateTime time, Pageable pageable);

    List<Booking> findAllByBookerIdAndStatus(long bookerId, BookingStatus status, Pageable pageable);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "WHERE b.startBooking < CURRENT_TIMESTAMP " +
            "AND b.endBooking > CURRENT_TIMESTAMP " +
            "AND b.item.ownerId = ?1")
    List<Booking> findAllCurrentBookingsByOwner(long ownerId, Pageable pageable);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "WHERE b.endBooking < CURRENT_TIMESTAMP " +
            "AND b.item.ownerId = ?1")
    List<Booking> findAllPastBookingsByOwner(long ownerId, Pageable pageable);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "WHERE b.startBooking > CURRENT_TIMESTAMP " +
            "AND b.item.ownerId = ?1")
    List<Booking> findAllFutureBookingsByOwner(long ownerId, Pageable pageable);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "WHERE b.status = ?2 " +
            "AND b.item.ownerId = ?1")
    List<Booking> findAllBookingsByOwnerAndStatus(long ownerId, BookingStatus status, Pageable pageable);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "WHERE b.item.ownerId = ?1")
    List<Booking> findAllBookingsByOwner(long ownerId, Pageable pageable);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "WHERE b.item.id = ?1 " +
            "AND b.endBooking < CURRENT_TIMESTAMP " +
            "ORDER BY b.endBooking DESC")
    List<Booking> findPastBookingsByItemId(long itemId);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "WHERE b.item.id = ?1 " +
            "AND b.startBooking > CURRENT_TIMESTAMP " +
            "ORDER BY b.endBooking ASC")
    List<Booking> findFutureBookingsByItemId(long itemId);

    List<Booking> findAllByItemId(long itemId);
}
