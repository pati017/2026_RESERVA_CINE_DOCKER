package com.cinema.booking_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Todos los errores salen como ProblemDetail (RFC 7807), sin clases ApiError propias. */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage());
    }

    @ExceptionHandler(BookingAccessDeniedException.class)
    public ProblemDetail handleAccessDenied(BookingAccessDeniedException ex) {
        return problem(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage());
    }

    /** Violaciones de reglas de negocio → 422 Unprocessable Entity. */
    @ExceptionHandler({InvalidBookingStateException.class, NoSeatsAvailableException.class,
            ScreeningUnavailableException.class})
    public ProblemDetail handleBusinessRule(RuntimeException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Business Rule Violation", ex.getMessage());
    }

    /** movie-service caído tras agotar los reintentos (el SAGA ya compensó). */
    @ExceptionHandler(MovieServiceUnavailableException.class)
    public ProblemDetail handleDownstreamUnavailable(MovieServiceUnavailableException ex) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Movie Service Unavailable", ex.getMessage());
    }

    /** Circuit Breaker abierto: se rechaza sin llamar a movie-service. */
    @ExceptionHandler(MovieServiceCircuitOpenException.class)
    public ProblemDetail handleCircuitOpen(MovieServiceCircuitOpenException ex) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Circuit Breaker Open", ex.getMessage());
    }

    @ExceptionHandler(MovieServiceRejectedException.class)
    public ProblemDetail handleDownstreamRejected(MovieServiceRejectedException ex) {
        return problem(HttpStatus.BAD_GATEWAY, "Movie Service Rejected The Call", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errors.putIfAbsent(err.getField(), err.getDefaultMessage()));
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Bad Request", "Validation failed");
        pd.setProperty("errors", errors);
        return pd;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Bad Request", "Cuerpo de la petición ausente o con formato inválido");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Bad Request",
                "Parámetro '" + ex.getName() + "' con valor inválido: " + ex.getValue());
    }

    /** Resto de excepciones: las de Spring MVC (405, 415, 404 de ruta…) ya traen su ProblemDetail. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex) {
        if (ex instanceof ErrorResponse er) {
            return ResponseEntity.status(er.getStatusCode()).headers(er.getHeaders()).body(er.getBody());
        }
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Error interno inesperado"));
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        return pd;
    }
}
