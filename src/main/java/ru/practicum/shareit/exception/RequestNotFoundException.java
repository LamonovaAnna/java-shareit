package ru.practicum.shareit.exception;

public class RequestNotFoundException extends RuntimeException {
    public RequestNotFoundException() {
        super("This request doesn't exist");
    }
}
