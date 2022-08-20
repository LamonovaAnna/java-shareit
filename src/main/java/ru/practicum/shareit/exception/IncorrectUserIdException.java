package ru.practicum.shareit.exception;

public class IncorrectUserIdException extends RuntimeException {
    public IncorrectUserIdException() {
        super("Access error. Only the owner can make changes");
    }
}
