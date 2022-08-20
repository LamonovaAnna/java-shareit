package ru.practicum.shareit.exception;

public class ErrorResponse {
    private final String errorDescription;

    public ErrorResponse(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public String getErrorDescription() {
        return errorDescription;
    }
}