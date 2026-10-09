package com.cinema.movie_service.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Función (proyección) de una película")
public class ScreeningResponse {
    @Schema(description = "Identificador", example = "73d2a76f-005a-4b96-b072-cd414e5b22b6")
    private UUID id;
    @Schema(description = "Película", example = "d508494b-4b2a-431c-99d9-bb4fc27de754")
    private UUID movieId;
    @Schema(description = "Título de la película (booking-service lo copia como snapshot)", example = "Toy Story")
    private String movieTitle;
    @Schema(description = "Sala", example = "Sala Infantil")
    private String room;
    @Schema(description = "Fecha y hora de la función", example = "2026-10-10T15:00:00")
    private LocalDateTime showAt;
    @Schema(description = "Capacidad total", example = "45")
    private Integer totalSeats;
    @Schema(description = "Asientos disponibles (nunca negativo)", example = "45")
    private Integer availableSeats;
    @Schema(description = "Si la función está activa", example = "true")
    private boolean active;
}
