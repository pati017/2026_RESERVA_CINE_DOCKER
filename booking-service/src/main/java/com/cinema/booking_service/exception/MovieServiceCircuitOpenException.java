package com.cinema.booking_service.exception;

/**
 * Fallback del Circuit Breaker: el circuito hacia movie-service está ABIERTO y la llamada se rechazó
 * sin intentarla (503). Es deliberadamente distinta de MovieServiceUnavailableException para que @Retry
 * NO la reintente (reintentar contra un circuito abierto no tiene sentido).
 */
public class MovieServiceCircuitOpenException extends RuntimeException {
    public MovieServiceCircuitOpenException(String message) {
        super(message);
    }
}
