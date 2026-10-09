package com.cinema.booking_service.application;

import java.util.UUID;

/** Quién hace la petición, derivado del JWT (el cliente ya no envía su propio customerId). */
public record Requester(UUID customerId, boolean admin) {
}
