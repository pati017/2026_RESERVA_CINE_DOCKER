package com.cinema.booking_service.exception;

/** movie-service rechazó la llamada con un 4xx inesperado: 401, 403, 404... No se reintenta (502) */
public class MovieServiceRejectedException extends RuntimeException {
    public MovieServiceRejectedException(String message) {
        super(message);
    }
}
