package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto createItem(ItemDto itemDto, long userId) {
        checkItemValid(itemDto);
        checkUserExist(userId);
        return ItemMapper.toItemDto(itemRepository.save(ItemMapper.toItem(itemDto, userId)));
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, long userId, long itemId) {
        checkUserExist(userId);
        if (itemRepository.getReferenceById(itemId).getOwnerId() != userId) {
            log.info("Incorrect user id");
            throw new IncorrectUserIdException();
        }
        return ItemMapper.toItemDto(itemRepository.save(ItemMapper.toUpdateItem(
                itemRepository.getReferenceById(itemId), ItemMapper.toItem(itemDto, userId))));
    }

    @Override
    public List<ItemBookingDto> getAllItemsByOwner(long userId, Integer from, Integer size) {
        checkUserExist(userId);
        checkPaginationParametersAreCorrect(from, size);

        List<ItemBookingDto> items = ItemMapper.toItemsBookingDto(itemRepository.findAllByOwnerId(
                userId, PageRequest.of(from / size, size)));
        for (ItemBookingDto item : items) {
            List<Booking> lastBookings = bookingRepository.findPastBookingsByItemId(item.getId());
            List<Booking> nextBookings = bookingRepository.findFutureBookingsByItemId(item.getId());
            if (!lastBookings.isEmpty()) {
                item.setLastBooking(BookingMapper.toBookingForItemDto(lastBookings.get(0)));
            }
            if (!nextBookings.isEmpty()) {
                item.setNextBooking(BookingMapper.toBookingForItemDto(nextBookings.get(0)));
            }
        }

        return items
                .stream()
                .sorted(Comparator.comparingLong(ItemBookingDto::getId))
                .collect(Collectors.toList());
    }

    @Override
    public ItemBookingDto findItemById(long itemId, Long userId) {
        checkUserExist(userId);
        ItemBookingDto item = ItemMapper.toItemBookingDto(itemRepository.findById(itemId)
                .orElseThrow(ItemNotFoundException::new));
        if (Objects.equals(item.getOwnerId(), userId)) {
            List<Booking> lastBookings = bookingRepository.findPastBookingsByItemId(itemId);
            List<Booking> nextBookings = bookingRepository.findFutureBookingsByItemId(itemId);
            if (!lastBookings.isEmpty()) {
                item.setLastBooking(BookingMapper.toBookingForItemDto(lastBookings.get(0)));
            }
            if (!nextBookings.isEmpty()) {
                item.setNextBooking(BookingMapper.toBookingForItemDto(nextBookings.get(0)));
            }
        }
        Set<Comment> comments = commentRepository.findCommentsByItem_Id(itemId);
        if (!comments.isEmpty()) {
            item.setComments(CommentMapper.toCommentsDto(comments));
        }
        return item;
    }

    @Override
    public void deleteItem(long itemId, long userId) {
        checkUserExist(userId);
        if (itemRepository.getReferenceById(itemId).getOwnerId() != userId) {
            log.info("Incorrect user id");
            throw new IncorrectUserIdException();
        }
        itemRepository.deleteById(itemId);
    }

    @Override
    public List<ItemDto> findItemsByNameOrDescription(String text, Integer from, Integer size) {
        checkPaginationParametersAreCorrect(from, size);
        if (text != null && !text.isBlank()) {
            String textForSearch = text.toLowerCase();
            return ItemMapper.toItemsDto(itemRepository.findAll(
                            PageRequest.of(from / size, size)).stream().collect(Collectors.toList()))
                    .stream()
                    .filter(i -> i.getName().toLowerCase().contains(textForSearch)
                            || i.getDescription().toLowerCase().contains(textForSearch))
                    .filter(ItemDto::getIsAvailable)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public CommentDto createCommentToItem(long authorId, CommentDto commentDto, long itemId) {
        if (userRepository.findById(authorId).isEmpty()) {
            throw new UserNotFoundException();
        }
        if (commentDto.getText().isBlank() || commentDto.getText().isEmpty()) {
            throw new ValidationException("Comment can't be empty");
        }
        if (!itemRepository.existsById(itemId)) {
            throw new IncorrectUserIdException();
        }
        List<Booking> bookings = bookingRepository.findAllByItemId(itemId);
        if (bookings.isEmpty()) {
            throw new BookingNotFoundException();
        }
        CommentDto comment = null;
        for (Booking booking : bookings) {
            if (booking.getBooker().getId() == authorId && booking.getEndBooking().isBefore(LocalDateTime.now())) {
                comment = CommentMapper.toCommentDto(commentRepository.save(
                        CommentMapper.toComment(commentDto, authorId, itemId)));
                comment.setItem(ItemMapper.toItemShortDto(itemRepository.getReferenceById(itemId)));
                comment.setAuthorName(userRepository.findById(authorId).get().getName());
            }
        }
        if (comment == null) {
            throw new ValidationException("User has not reserved this item");
        }
        return comment;
    }

    private void checkItemValid(ItemDto item) {
        if (item.getName() == null || item.getName().isBlank()) {
            log.info("Field \"name\" doesn't filled");
            throw new ValidationException("Incorrect item name");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            log.info("Field \"description\" doesn't filled");
            throw new ValidationException("Incorrect item description");
        }
        if (item.getIsAvailable() == null) {
            log.info("Field \"available\" doesn't filled");
            throw new ValidationException("Field \"isAvailable\" must be filled");
        }
    }

    private void checkPaginationParametersAreCorrect(Integer from, Integer size) {
        if (from < 0) {
            log.info("Parameter \"from\" have to be above or equals zero");
            throw new ValidationException("Incorrect parameter \"from\"");
        }
        if (size <= 0) {
            log.info("Parameter \"size\" have to be above zero");
            throw new ValidationException("Incorrect parameter \"size\"");
        }
    }

    private void checkUserExist(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.info("Incorrect user id");
            throw new UserNotFoundException();
        }
    }
}