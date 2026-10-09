package com.cinema.booking_service.api;

import com.cinema.booking_service.api.dto.BookingRequest;
import com.cinema.booking_service.api.dto.BookingResponse;
import com.cinema.booking_service.application.BookingService;
import com.cinema.booking_service.application.Requester;
import com.cinema.booking_service.domain.Booking;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Reservas de entradas — ejecutan el SAGA contra movie-service")
public class BookingController {

    private final BookingService service;

    @PostMapping
    @Operation(summary = "Crear una reserva (ejecuta el SAGA de reserva)",
            description = "1) verifica la función, 2) crea la reserva PENDING, 3) descuenta el asiento en movie-service "
                    + "(con @Retry), 4) confirma. Si algo falla se compensa. Devuelve 201 con header Location.")
    @ApiResponse(responseCode = "201", description = "Reserva CONFIRMED")
    @ApiResponse(responseCode = "400", description = "Datos inválidos",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "Sin asientos, función inexistente/inactiva o no verificable",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "502", description = "movie-service rechazó la llamada (reserva compensada)",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "503", description = "movie-service no disponible tras los reintentos (reserva compensada)",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request,
                                                         @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        Requester requester = requesterFrom(jwt);
        Booking saved = service.createBooking(request.getScreeningId(), requester.customerId(), request.getTotalAmount());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).body(toResponse(saved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una reserva por ID",
            description = "Requiere JWT. Solo su dueño (o un ADMIN) puede verla. No llama a movie-service: usa el snapshot.")
    @ApiResponse(responseCode = "200", description = "Reserva encontrada")
    @ApiResponse(responseCode = "400", description = "El id no es un UUID válido",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "La reserva pertenece a otro usuario",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Reserva no encontrada",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable UUID id,
                                                          @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(toResponse(service.getById(id, requesterFrom(jwt))));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar una reserva (SAGA de cancelación)",
            description = "Marca CANCELLED y libera el asiento en movie-service (best-effort: si la liberación falla "
                    + "se registra el error y la reserva queda CANCELLED de todos modos).")
    @ApiResponse(responseCode = "200", description = "Reserva cancelada")
    @ApiResponse(responseCode = "400", description = "El id no es un UUID válido",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "La reserva pertenece a otro usuario",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Reserva no encontrada",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "La reserva ya estaba cancelada",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable UUID id,
                                                         @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(toResponse(service.cancelBooking(id, requesterFrom(jwt))));
    }

    /**
     * Identidad tomada del JWT: auth-service emite sub = username, así que el customerId es un UUID
     * determinista derivado de ese sub (mismo usuario → mismo customerId, sin tabla de usuarios en booking).
     * El rol llega en el claim "roles" (p.ej. ["ROLE_ADMIN"]).
     */
    private Requester requesterFrom(Jwt jwt) {
        UUID customerId = UUID.nameUUIDFromBytes(("cinema-customer:" + jwt.getSubject()).getBytes(StandardCharsets.UTF_8));
        List<String> roles = jwt.getClaimAsStringList("roles");
        boolean admin = roles != null && roles.contains("ROLE_ADMIN");
        return new Requester(customerId, admin);
    }

    private BookingResponse toResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .screeningId(booking.getScreeningId())
                .movieTitle(booking.getMovieTitle())
                .customerId(booking.getCustomerId())
                .status(booking.getStatus())
                .totalAmount(booking.getTotalAmount())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
