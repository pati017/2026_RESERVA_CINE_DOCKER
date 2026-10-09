package com.cinema.booking_service.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingConfirmedEventTest {

    @Test
    void jsonContainsAllFieldsAndEscapesQuotes() {
        UUID id = UUID.randomUUID();
        BookingConfirmedEvent e = new BookingConfirmedEvent(id, UUID.randomUUID(), "El \"Gran\" Viaje",
                UUID.randomUUID(), new BigDecimal("15.50"), Instant.parse("2026-10-01T14:30:00Z"));

        String json = e.toJson();

        assertTrue(json.contains("\"eventType\":\"BookingConfirmedEvent\""));
        assertTrue(json.contains("\"bookingId\":\"" + id + "\""));
        assertTrue(json.contains("\"movieTitle\":\"El \\\"Gran\\\" Viaje\""));
        assertTrue(json.contains("\"totalAmount\":15.50"));
        assertTrue(json.contains("\"occurredAt\":\"2026-10-01T14:30:00Z\""));
    }
}
