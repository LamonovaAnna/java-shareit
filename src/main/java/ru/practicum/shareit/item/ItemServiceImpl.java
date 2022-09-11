package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.exception.IncorrectUserIdException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Repository.CommentRepository;
import ru.practicum.shareit.item.Repository.ItemRepository;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto createItem(ItemDto itemDto, long userId) {
        if (userService.findUserById(userId) != null && isItemValid(itemDto)) {
            return ItemMapper.toItemDto(itemRepository.save(ItemMapper.toItem(itemDto, userId)));
        }
        log.info("User not found");
        throw new UserNotFoundException();
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, long userId, long itemId) {
        if (userService.findUserById(userId) != null &&
                itemRepository.getReferenceById(itemId).getOwnerId() == userId) {
            return ItemMapper.toItemDto(itemRepository.save(ItemMapper.toUpdateItem(
                    itemRepository.getReferenceById(itemId), ItemMapper.toItem(itemDto, userId))));
        }
        log.info("Incorrect user id");
        throw new IncorrectUserIdException();
    }

    @Override
    public List<ItemBookingDto> getAllItemsByOwner(long userId) {
        userService.findUserById(userId);
        List<ItemBookingDto> items = ItemMapper.toItemsBookingDto(itemRepository.findAllByOwnerId(userId));
        for (ItemBookingDto item : items) {
            List<Booking> lastBookings = bookingRepository
                    .findBookingByItemIdAndEndBookingIsBeforeOrderByEndBookingDesc(item.getId(), LocalDateTime.now());
            List<Booking> nextBookings = bookingRepository
                    .findBookingByItemIdAndStartBookingIsAfterOrderByStartBookingAsc(item.getId(), LocalDateTime.now());
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
        if (!itemRepository.existsById(itemId)) {
            throw new ItemNotFoundException();
        }

        ItemBookingDto item = ItemMapper.toItemBookingDto(itemRepository.getReferenceById(itemId));
        if (userService.findUserById(userId) != null) {
            if (Objects.equals(item.getOwnerId(), userId)) {
                List<Booking> lastBookings = bookingRepository
                        .findBookingByItemIdAndEndBookingIsBeforeOrderByEndBookingDesc(itemId, LocalDateTime.now());
                List<Booking> nextBookings = bookingRepository
                        .findBookingByItemIdAndStartBookingIsAfterOrderByStartBookingAsc(itemId, LocalDateTime.now());
                if (!lastBookings.isEmpty()) {
                    item.setLastBooking(BookingMapper.toBookingForItemDto(lastBookings.get(0)));
                }
                if (!nextBookings.isEmpty()) {
                    item.setNextBooking(BookingMapper.toBookingForItemDto(nextBookings.get(0)));
                }
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
        if (userService.findUserById(userId) != null &&
                itemRepository.getReferenceById(itemId).getOwnerId() == userId) {
            itemRepository.deleteById(itemId);
        }
        log.info("Incorrect user id");
        throw new IncorrectUserIdException();
    }

    @Override
    public List<ItemDto> findItemsByNameOrDescription(String text) {
        if (text != null && !text.isBlank()) {
            String textForSearch = text.toLowerCase();
            return ItemMapper.toItemsDto(itemRepository.findAll())
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
        userService.findUserById(authorId);
        if (commentDto.getText().isBlank() || commentDto.getText().isEmpty()) {
            throw new ValidationException("Comment can't be empty");
        }

        if (itemRepository.existsById(itemId)) {
            List<Booking> bookings = bookingRepository.findAllByItemId(itemId);
            if (!bookings.isEmpty()) {
                for (Booking booking : bookings) {
                    if (booking.getBooker().getId() == authorId &&
                            booking.getEndBooking().isBefore(LocalDateTime.now())) {
                        CommentDto comment = CommentMapper.toCommentDto(commentRepository.save(
                                CommentMapper.toComment(commentDto, authorId, itemId)));
                        comment.setItem(ItemMapper.toItemShortDto(itemRepository.getReferenceById(itemId)));
                        comment.setAuthorName(userService.findUserById(authorId).getName());
                        return comment;
                    }
                }
            }
            throw new ValidationException("User has not reserved this item");
        }
        throw new IncorrectUserIdException();
    }

    private boolean isItemValid(ItemDto item) {
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
        return true;
    }
}