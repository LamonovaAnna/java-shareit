package ru.practicum.shareit.exception;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException() {
        super("This item doesn't exist");
    }
}
