package com.cinema.movie_service.api.dto;

import com.cinema.movie_service.domain.Genre;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "Datos para crear o actualizar una película del catálogo")
public class MovieRequest {

    @NotBlank
    @Schema(description = "Título de la película", example = "Kung Fu Panda")
    private String title;

    @NotBlank
    @Schema(description = "Director", example = "Mark Osborne")
    private String director;

    @NotNull
    @Schema(description = "Género", example = "ANIMATION")
    private Genre genre;

    @Positive
    @Schema(description = "Duración en minutos", example = "92")
    private Integer durationMin;

    @Schema(description = "Clasificación por edad", example = "PG")
    private String rating;
}
