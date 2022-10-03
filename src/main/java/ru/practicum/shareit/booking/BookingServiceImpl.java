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
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;


    @Override
    public BookingShortDto createBooking(BookingShortDto bookingDto, long bookerId) {
        checkUserExist(bookerId);
        bookingValidation(bookingDto, bookerId);
        bookingDto.setBookerId(bookerId);
        return BookingMapper.toBookingShortDto(bookingRepository.save(BookingMapper.toBooking(bookingDto)));
    }

    @Override
    public BookingDto findBookingById(long bookingId, long userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(BookingNotFoundException::new);
        if (booking.getBooker().getId() != userId && booking.getItem().getOwnerId() != userId) {
            log.info("Incorrect user id {}", userId);
            throw new IncorrectUserIdException();
        }
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> findBookingsByBooker(long bookerId, String state, Integer from, Integer size) {
        checkUserExist(bookerId);
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
                return BookingMapper.toBookingsDto(bookingRepository.findAllByBookerIdAndStatus(bookerId,
                        BookingStatus.REJECTED, pageable));
            case "ALL":
                return BookingMapper.toBookingsDto(bookingRepository.findAllByBookerId(bookerId, pageable));
            default:
                log.info("Incorrect state: {}", state);
                throw new IncorrectStatusException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    @Override
    public List<BookingDto> findBookingsByOwner(long ownerId, String state, Integer from, Integer size) {
        checkUserExist(ownerId);
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
                return BookingMapper.toBookingsDto(bookingRepository.findAllBookingsByOwnerAndStatus(ownerId,
                        BookingStatus.REJECTED, pageable));
            case "ALL":
                return BookingMapper.toBookingsDto(bookingRepository.findAllBookingsByOwner(ownerId, pageable));
            default:
                log.info("Incorrect state: {}", state);
                throw new IncorrectStatusException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    @Override
    public BookingDto approveOrRejectBooking(long ownerId, long bookingId, boolean isApproved) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(BookingNotFoundException::new);
        checkUserExist(ownerId);
        checkIsOwner(booking, ownerId);

        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new IncorrectStatusException("Booking's status was updated earlier");
        }
        if (!isApproved) {
            booking.setStatus(BookingStatus.REJECTED);
        } else {
            booking.setStatus(BookingStatus.APPROVED);
        }
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    private boolean isDateValid(BookingShortDto bookingDto) {
        if (bookingDto.getEnd() != null && bookingDto.getEnd().isBefore(bookingDto.getStart()) ||
                bookingDto.getEnd().equals(bookingDto.getStart())) {
            log.info("Incorrect end time {}", bookingDto.getEnd());
            return false;
        }
        if (bookingDto.getStart() != null && bookingDto.getStart().isBefore(LocalDateTime.now())) {
            log.info("Incorrect start time {}", bookingDto.getStart());
            return false;
        }
        return true;
    }

    private void checkIsOwner(Booking booking, long ownerId) {
        if (booking.getItem().getOwnerId() != ownerId) {
            log.info("Incorrect user id {}", ownerId);
            throw new IncorrectUserIdException();
        }
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

    private void checkUserExist(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.info("Incorrect user id {}", userId);
            throw new UserNotFoundException();
        }
    }

    private void bookingValidation(BookingShortDto bookingDto, long bookerId) {
        if (itemRepository.findById(bookingDto.getItemId()).isEmpty()) {
            log.info("Item with id {} not found", bookingDto.getItemId());
            throw new ItemNotFoundException();
        } else if (itemRepository.findById(bookingDto.getItemId()).get().getOwnerId() == bookerId) {
            log.info("Owner cannot reserve his own item");
            throw new IncorrectUserIdException();
        } else if (!itemRepository.findById(bookingDto.getItemId()).get().getIsAvailable()) {
            log.info("Item with id {} doesn't available", bookingDto.getItemId());
            throw new ItemNotAvailableException();
        } else if (!isDateValid(bookingDto)) {
            throw new ValidationException("Incorrect start or end time");
        }
    }
}