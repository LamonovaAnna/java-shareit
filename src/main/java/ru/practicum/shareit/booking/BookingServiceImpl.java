package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.Repository.ItemRepository;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;

    @Override
    public BookingShortDto createBooking(BookingShortDto bookingDto, long bookerId) {
        userService.findUserById(bookerId);

        if (!itemRepository.existsById(bookingDto.getItemId())) {
            log.info("Item not found");
            throw new ItemNotFoundException();
        } else if (itemRepository.getReferenceById(bookingDto.getItemId()).getOwnerId() == bookerId) {
            log.info("Owner cannot reserve his own item");
            throw new IncorrectUserIdException();
        } else if (!itemRepository.getReferenceById(bookingDto.getItemId()).getIsAvailable()) {
            log.info("Item doesn't available");
            throw new ItemNotAvailableException();
        } else if (!isDateValid(bookingDto)) {
            throw new ValidationException("Incorrect start or end time");
        }

        bookingDto.setBookerId(bookerId);
        return BookingMapper.toBookingShortDto(bookingRepository.save(BookingMapper.toBooking(bookingDto)));
    }

    @Override
    public BookingDto findBookingById(long bookingId, long userId) {
        if (!bookingRepository.existsById(bookingId)) {
            log.info("Booking not found");
            throw new BookingNotFoundException();
        }

        Booking booking = bookingRepository.getReferenceById(bookingId);
        if (!Objects.equals(booking.getBooker().getId(), userId) && !isOwner(booking, userId)) {
            log.info("Incorrect user id");
            throw new IncorrectUserIdException();
        }
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> findBookingsByBooker(long bookerId, String state, Integer from, Integer size) {
        userService.findUserById(bookerId);
        checkPaginationParametersAreCorrect(from, size);
        PageRequest pageable = PageRequest.of(from / size, size, Sort.by("startBooking").descending());

        switch (state) {
            case "CURRENT":
                return BookingMapper.toBookingsDto(
                        bookingRepository.findAllCurrentBookingsByBookerId(bookerId, pageable));
            case "PAST":
                return BookingMapper.toBookingsDto(
                        bookingRepository.findAllByBookerIdAndEndBookingIsBefore(
                                bookerId, LocalDateTime.now(), pageable));
            case "FUTURE":
                return BookingMapper.toBookingsDto(
                        bookingRepository.findAllByBookerIdAndStartBookingIsAfter(
                        bookerId, LocalDateTime.now(), pageable));
            case "WAITING":
                return BookingMapper.toBookingsDto(bookingRepository.findAllByBookerIdAndStatus(
                        bookerId, BookingStatus.WAITING, pageable));
            case "REJECTED":
                return BookingMapper.toBookingsDto(bookingRepository.findAllByBookerIdAndStatus(
                        bookerId, BookingStatus.REJECTED, pageable));
            case "ALL":
                return BookingMapper.toBookingsDto(bookingRepository.findAllByBookerId(bookerId, pageable));
            default:
                log.info("Incorrect state: {}", state);
                throw new IncorrectStatusException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    @Override
    public List<BookingDto> findBookingsByOwner(long ownerId, String state,Integer from, Integer size) {
        userService.findUserById(ownerId);
        checkPaginationParametersAreCorrect(from, size);
        PageRequest pageable = PageRequest.of(from / size, size, Sort.by("startBooking").descending());

        switch (state) {
            case "CURRENT":
                return BookingMapper.toBookingsDto(bookingRepository.findAllCurrentBookingsByOwner(ownerId, pageable));
            case "PAST":
                return BookingMapper.toBookingsDto(bookingRepository.findAllPastBookingsByOwner(ownerId, pageable));
            case "FUTURE":
                return BookingMapper.toBookingsDto(bookingRepository.findAllFutureBookingsByOwner(ownerId, pageable));
            case "WAITING":
                return BookingMapper.toBookingsDto(bookingRepository.findAllBookingsByOwnerAndStatus(
                        ownerId, BookingStatus.WAITING, pageable));
            case "REJECTED":
                return BookingMapper.toBookingsDto(bookingRepository.findAllBookingsByOwnerAndStatus(
                        ownerId, BookingStatus.REJECTED, pageable));
            case "ALL":
                return BookingMapper.toBookingsDto(bookingRepository.findAllBookingsByOwner(ownerId, pageable));
            default:
                log.info("Incorrect state: {}", state);
                throw new IncorrectStatusException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    @Override
    public BookingDto approveOrRejectBooking(long ownerId, long bookingId, boolean isApproved) {
        if (bookingRepository.existsById(bookingId)) {
            Booking booking = bookingRepository.getReferenceById(bookingId);
            userService.findUserById(ownerId);

            if (!isOwner(booking, ownerId)) {
                log.info("Incorrect user id");
                throw new IncorrectUserIdException();
            } else if (!booking.getStatus().equals(BookingStatus.WAITING)) {
                throw new IncorrectStatusException("Booking's status was updated earlier");
            } else {
                if (!isApproved) {
                    booking.setStatus(BookingStatus.REJECTED);
                } else {
                    booking.setStatus(BookingStatus.APPROVED);
                }
                return BookingMapper.toBookingDto(bookingRepository.save(booking));
            }
        }
        log.info("Booking not found");
        throw new BookingNotFoundException();
    }

    private boolean isDateValid(BookingShortDto bookingDto) {
        if (bookingDto.getStart() != null || bookingDto.getEnd() != null) {
            if (bookingDto.getStart().isBefore(LocalDateTime.now())) {
                log.info("Incorrect start time");
                return false;
            } else if (bookingDto.getEnd().isBefore(bookingDto.getStart()) ||
                    bookingDto.getEnd().equals(bookingDto.getStart())) {
                log.info("Incorrect end time");
                return false;
            }
            return true;
        }
        return true;
    }

    private boolean isOwner(Booking booking, long ownerId) {
        return booking.getItem().getOwnerId() == ownerId;
    }

    private void checkPaginationParametersAreCorrect(Integer from, Integer size) {
        if (from != null && from < 0) {
            log.info("Parameter \"from\" have to be above or equals zero");
            throw new ValidationException("Incorrect parameter \"from\"");
        }
        if (size != null && size <= 0) {
            log.info("Parameter \"size\" have to be above zero");
            throw new ValidationException("Incorrect parameter \"size\"");
        }
    }
}