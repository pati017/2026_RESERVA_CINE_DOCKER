package com.cinema.booking_service.exception;

/** La función no tiene asientos disponibles (422) */
public class NoSeatsAvailableException extends RuntimeException {
    public NoSeatsAvailableException(String message) {
        super(message);
    }
}
