package ru.practicum.error.exeption;

public class RequestException extends RuntimeException {
    public RequestException(String message) {
        super(message);
    }
}