package com.cinema.movie_service.api.dto;

import com.cinema.movie_service.domain.Genre;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Película del catálogo")
public class MovieResponse {
    @Schema(description = "Identificador", example = "d508494b-4b2a-431c-99d9-bb4fc27de754")
    private UUID id;
    @Schema(description = "Título", example = "Toy Story")
    private String title;
    @Schema(description = "Director", example = "John Lasseter")
    private String director;
    @Schema(description = "Género", example = "ANIMATION")
    private Genre genre;
    @Schema(description = "Duración en minutos", example = "81")
    private Integer durationMin;
    @Schema(description = "Clasificación por edad", example = "G")
    private String rating;
    @Schema(description = "Si está activa en cartelera", example = "true")
    private boolean active;
    @Schema(description = "Fecha de creación (UTC)", example = "2026-10-01T14:30:00Z")
    private Instant createdAt;
    @Schema(description = "Última modificación (UTC)", example = "2026-10-01T14:30:00Z")
    private Instant updatedAt;
}
