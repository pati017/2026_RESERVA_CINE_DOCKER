package com.cinema.booking_service.api.dto;

import com.cinema.booking_service.domain.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Reserva de un cliente para una función")
public class BookingResponse {
    @Schema(description = "Identificador de la reserva", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;
    @Schema(description = "Función reservada", example = "73d2a76f-005a-4b96-b072-cd414e5b22b6")
    private UUID screeningId;
    @Schema(description = "Título de la película al momento de reservar (snapshot, no se actualiza)", example = "Toy Story")
    private String movieTitle;
    @Schema(description = "Cliente (derivado del claim sub del JWT)", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
    private UUID customerId;
    @Schema(description = "Estado de la reserva", example = "CONFIRMED")
    private BookingStatus status;
    @Schema(description = "Precio total", example = "15.50")
    private BigDecimal totalAmount;
    @Schema(description = "Fecha de creación (UTC)", example = "2026-10-01T14:30:00Z")
    private Instant createdAt;
    @Schema(description = "Última modificación (UTC)", example = "2026-10-01T14:30:05Z")
    private Instant updatedAt;
}
