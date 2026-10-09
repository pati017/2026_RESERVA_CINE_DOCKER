package com.cinema.booking_service.exception;

/** Operación no permitida para el estado actual de la reserva (422) */
public class InvalidBookingStateException extends RuntimeException {
    public InvalidBookingStateException(String message) {
        super(message);
    }
}
