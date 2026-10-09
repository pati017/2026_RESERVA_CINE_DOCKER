package com.cinema.booking_service.exception;

/** El usuario autenticado no es dueño de la reserva (403) */
public class BookingAccessDeniedException extends RuntimeException {
    public BookingAccessDeniedException(String message) {
        super(message);
    }
}
