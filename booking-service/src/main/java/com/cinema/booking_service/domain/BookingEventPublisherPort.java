package com.cinema.booking_service.domain;

/** Puerto de salida para publicar eventos de reservas (la implementación Kafka vive en infrastructure/messaging). */
public interface BookingEventPublisherPort {

    /** Best-effort: una implementación no debe lanzar excepciones que afecten al SAGA ya completado. */
    void publishBookingConfirmed(BookingConfirmedEvent event);
}
