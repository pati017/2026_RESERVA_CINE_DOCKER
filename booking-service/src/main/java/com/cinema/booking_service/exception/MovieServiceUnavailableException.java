package com.cinema.booking_service.exception;

/** Fallo transitorio de red / 5xx al hablar con movie-service. Es la ÚNICA que se reintenta (503) */
public class MovieServiceUnavailableException extends RuntimeException {
    public MovieServiceUnavailableException(String message) {
        super(message);
    }
}
