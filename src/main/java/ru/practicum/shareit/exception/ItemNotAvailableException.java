package ru.practicum.shareit.exception;

public class ItemNotAvailableException extends RuntimeException {
    public ItemNotAvailableException() {
        super("This item doesn't available");
    }
}
