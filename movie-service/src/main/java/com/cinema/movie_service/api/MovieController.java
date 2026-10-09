package com.cinema.movie_service.api;

import com.cinema.movie_service.api.dto.MovieRequest;
import com.cinema.movie_service.api.dto.MovieResponse;
import com.cinema.movie_service.application.MovieService;
import com.cinema.movie_service.domain.Movie;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
@Tag(name = "Movies", description = "Catálogo de películas")
public class MovieController {

    private final MovieService service;

    @PostMapping
    @Operation(summary = "Crear una película", description = "Requiere JWT. Devuelve 201 con header Location.")
    @ApiResponse(responseCode = "201", description = "Película creada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<MovieResponse> createMovie(@Valid @RequestBody MovieRequest request) {
        Movie movie = Movie.builder()
                .title(request.getTitle())
                .director(request.getDirector())
                .genre(request.getGenre())
                .durationMin(request.getDurationMin())
                .rating(request.getRating())
                .build();
        Movie saved = service.create(movie);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).body(toResponse(saved));
    }

    @GetMapping
    @Operation(summary = "Listar películas", description = "Público (no requiere token).")
    @ApiResponse(responseCode = "200", description = "Listado de películas")
    public ResponseEntity<List<MovieResponse>> getAllMovies() {
        return ResponseEntity.ok(service.getAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una película por ID", description = "Público (no requiere token).")
    @ApiResponse(responseCode = "200", description = "Película encontrada")
    @ApiResponse(responseCode = "400", description = "El id no es un UUID válido",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Película no encontrada",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(service.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una película", description = "Requiere JWT.")
    @ApiResponse(responseCode = "200", description = "Película actualizada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Película no encontrada",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<MovieResponse> updateMovie(@PathVariable UUID id, @Valid @RequestBody MovieRequest request) {
        Movie data = Movie.builder()
                .title(request.getTitle())
                .director(request.getDirector())
                .genre(request.getGenre())
                .durationMin(request.getDurationMin())
                .rating(request.getRating())
                .build();
        return ResponseEntity.ok(toResponse(service.update(id, data)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una película", description = "Requiere JWT. Elimina también sus funciones.")
    @ApiResponse(responseCode = "204", description = "Película eliminada")
    @ApiResponse(responseCode = "400", description = "El id no es un UUID válido",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Película no encontrada",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<Void> deleteMovie(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activar una película", description = "Requiere JWT.")
    @ApiResponse(responseCode = "200", description = "Película activada")
    @ApiResponse(responseCode = "400", description = "El id no es un UUID válido",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Película no encontrada",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<MovieResponse> activateMovie(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(service.activate(id)));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Desactivar una película", description = "Requiere JWT.")
    @ApiResponse(responseCode = "200", description = "Película desactivada")
    @ApiResponse(responseCode = "400", description = "El id no es un UUID válido",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Película no encontrada",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<MovieResponse> deactivateMovie(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(service.deactivate(id)));
    }

    private MovieResponse toResponse(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .director(movie.getDirector())
                .genre(movie.getGenre())
                .durationMin(movie.getDurationMin())
                .rating(movie.getRating())
                .active(movie.isActive())
                .createdAt(movie.getCreatedAt())
                .updatedAt(movie.getUpdatedAt())
                .build();
    }
}
