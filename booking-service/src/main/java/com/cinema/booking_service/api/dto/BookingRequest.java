package com.cinema.booking_service.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Schema(description = "Datos para reservar una entrada. El cliente se toma del JWT, no del cuerpo.")
public class BookingRequest {

    @NotNull
    @Schema(description = "Función que se quiere reservar", example = "73d2a76f-005a-4b96-b072-cd414e5b22b6")
    private UUID screeningId;

    @NotNull
    @Positive
    @Schema(description = "Precio total de la reserva", example = "15.50")
    private BigDecimal totalAmount;
}
