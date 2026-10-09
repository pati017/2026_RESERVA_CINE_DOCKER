package com.cinema.movie_service.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "Datos para programar una función")
public class ScreeningRequest {

    @NotNull
    @Schema(description = "Película que se proyecta", example = "d508494b-4b2a-431c-99d9-bb4fc27de754")
    private UUID movieId;

    @NotBlank
    @Schema(description = "Sala", example = "Sala Infantil")
    private String room;

    @NotNull
    @Future
    @Schema(description = "Fecha y hora de la función (hora local de la sala)", example = "2026-11-20T21:00:00")
    private LocalDateTime showAt;

    @NotNull
    @Positive
    @Schema(description = "Capacidad de la sala (los asientos disponibles arrancan igual)", example = "45")
    private Integer totalSeats;
}
