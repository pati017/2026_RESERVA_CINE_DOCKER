package com.cinema.movie_service.exception;

public class ScreeningNotAvailableException extends RuntimeException {
    public ScreeningNotAvailableException(String message) {
        super(message);
    }
}
