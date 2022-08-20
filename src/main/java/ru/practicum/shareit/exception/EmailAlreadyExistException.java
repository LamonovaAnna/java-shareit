package ru.practicum.shareit.exception;

public class EmailAlreadyExistException extends RuntimeException {

    public EmailAlreadyExistException(String message) {
        super(message);
    }

    public String getMessage(String email) {
        return "User with email " + email + " is already exist.";
    }
}
