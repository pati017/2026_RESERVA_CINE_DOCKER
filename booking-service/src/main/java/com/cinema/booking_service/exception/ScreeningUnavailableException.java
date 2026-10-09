package com.cinema.booking_service.exception;

/** La función no existe, está inactiva o no pudo verificarse - paso 1 del SAGA (422) */
public class ScreeningUnavailableException extends RuntimeException {
    public ScreeningUnavailableException(String message) {
        super(message);
    }
}
