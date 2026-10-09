package com.cinema.booking_service.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Evento de dominio: el SAGA de reserva terminó bien (booking CONFIRMED). */
public record BookingConfirmedEvent(UUID bookingId, UUID screeningId, String movieTitle,
                                    UUID customerId, BigDecimal totalAmount, Instant occurredAt) {

    public static BookingConfirmedEvent from(Booking booking) {
        return new BookingConfirmedEvent(booking.getId(), booking.getScreeningId(), booking.getMovieTitle(),
                booking.getCustomerId(), booking.getTotalAmount(), Instant.now());
    }

    /** JSON escrito a mano: evita depender de la versión de Jackson (2 ó 3) y mantiene el dominio sin librerías. */
    public String toJson() {
        return "{\"eventType\":\"BookingConfirmedEvent\""
                + ",\"bookingId\":\"" + bookingId + "\""
                + ",\"screeningId\":\"" + screeningId + "\""
                + ",\"movieTitle\":\"" + escape(movieTitle) + "\""
                + ",\"customerId\":\"" + customerId + "\""
                + ",\"totalAmount\":" + (totalAmount == null ? "null" : totalAmount.toPlainString())
                + ",\"occurredAt\":\"" + occurredAt + "\"}";
    }

    private static String escape(String value) {
        if (value == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c < 0x20 ? String.format("\\u%04x", (int) c) : String.valueOf(c));
            }
        }
        return sb.toString();
    }
}
