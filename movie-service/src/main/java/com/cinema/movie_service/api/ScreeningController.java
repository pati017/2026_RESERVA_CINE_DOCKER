package com.cinema.movie_service.api;

import com.cinema.movie_service.api.dto.ScreeningRequest;
import com.cinema.movie_service.api.dto.ScreeningResponse;
import com.cinema.movie_service.application.ScreeningService;
import com.cinema.movie_service.application.ScreeningView;
import com.cinema.movie_service.domain.Screening;
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
@RequestMapping("/screenings")
@RequiredArgsConstructor
@Tag(name = "Screenings", description = "Funciones (proyecciones) y control de asientos")
public class ScreeningController {

    private final ScreeningService service;

    @PostMapping
    @Operation(summary = "Programar una función", description = "Requiere JWT. Devuelve 201 con header Location.")
    @ApiResponse(responseCode = "201", description = "Función creada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "La película no existe",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ScreeningResponse> createScreening(@Valid @RequestBody ScreeningRequest request) {
        ScreeningView saved = service.create(request.getMovieId(), request.getRoom(),
                request.getShowAt(), request.getTotalSeats());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.screening().getId())
                .toUri();

        return ResponseEntity.created(location).body(toResponse(saved));
    }

    @GetMapping
    @Operation(summary = "Listar funciones (cartelera)", description = "Público (no requiere token).")
    @ApiResponse(responseCode = "200", description = "Listado de funciones")
    public ResponseEntity<List<ScreeningResponse>> getAllScreenings() {
        return ResponseEntity.ok(service.getAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una función por ID", description = "Público (no requiere token).")
    @ApiResponse(responseCode = "200", description = "Función encontrada")
    @ApiResponse(responseCode = "400", description = "El id no es un UUID válido",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Función no encontrada",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ScreeningResponse> getScreeningById(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(service.getById(id)));
    }

    @PatchMapping("/{id}/reserve")
    @Operation(summary = "Reservar un asiento (decrementa availableSeats)",
            description = "Paso 3 del SAGA de reserva. Requiere JWT (booking-service reenvía el token del usuario).")
    @ApiResponse(responseCode = "200", description = "Asiento reservado")
    @ApiResponse(responseCode = "400", description = "El id no es un UUID válido",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Función no encontrada",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "Sin asientos disponibles o función inactiva",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ScreeningResponse> reserveSeat(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(service.reserveSeat(id)));
    }

    @PatchMapping("/{id}/release")
    @Operation(summary = "Liberar un asiento (incrementa availableSeats)",
            description = "Compensación del SAGA / cancelación de reserva. Requiere JWT.")
    @ApiResponse(responseCode = "200", description = "Asiento liberado")
    @ApiResponse(responseCode = "400", description = "El id no es un UUID válido",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Función no encontrada",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ScreeningResponse> releaseSeat(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(service.releaseSeat(id)));
    }

    private ScreeningResponse toResponse(ScreeningView view) {
        Screening screening = view.screening();
        return ScreeningResponse.builder()
                .id(screening.getId())
                .movieId(screening.getMovieId())
                .movieTitle(view.movieTitle())
                .room(screening.getRoom())
                .showAt(screening.getShowAt())
                .totalSeats(screening.getTotalSeats())
                .availableSeats(screening.getAvailableSeats())
                .active(screening.isActive())
                .build();
    }
}
