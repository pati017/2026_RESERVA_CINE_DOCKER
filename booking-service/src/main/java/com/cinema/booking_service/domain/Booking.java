package com.cinema.booking_service.domain;

import com.cinema.booking_service.exception.InvalidBookingStateException;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Agregado Booking. Las transiciones de estado (PENDING → CONFIRMED / CANCELLED) las controla
 * la propia entidad; el servicio de aplicación no puede dejarla en un estado inválido.
 */
@Getter
@Builder
public class Booking {
    private UUID id;
    private UUID screeningId;
    /** Snapshot: se copia de la función al reservar y no se actualiza si la película cambia después. */
    private String movieTitle;
    private UUID customerId;
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private Instant updatedAt;

    /** Paso 2 del SAGA: reserva nueva en estado PENDING. */
    public static Booking pending(UUID screeningId, String movieTitle, UUID customerId, BigDecimal totalAmount) {
        return Booking.builder()
                .screeningId(screeningId)
                .movieTitle(movieTitle)
                .customerId(customerId)
                .totalAmount(totalAmount)
                .status(BookingStatus.PENDING)
                .build();
    }

    /** Paso 4 del SAGA. Solo una reserva PENDING puede confirmarse. */
    public void confirm() {
        if (status != BookingStatus.PENDING) {
            throw new InvalidBookingStateException("Solo una reserva PENDING puede confirmarse (estado actual: " + status + ")");
        }
        status = BookingStatus.CONFIRMED;
    }

    /** Cancelación (usuario o compensación). Una reserva ya cancelada no puede cancelarse otra vez. */
    public void cancel() {
        if (status == BookingStatus.CANCELLED) {
            throw new InvalidBookingStateException("La reserva ya se encuentra cancelada");
        }
        status = BookingStatus.CANCELLED;
    }

    public boolean belongsTo(UUID customer) {
        return customerId != null && customerId.equals(customer);
    }
}
