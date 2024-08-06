package ru.practicum.error;

public class ExceptionResponse {
    private final String error;

    public ExceptionResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}

