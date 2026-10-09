package com.cinema.movie_service.application;

import com.cinema.movie_service.domain.Movie;
import com.cinema.movie_service.domain.Screening;
import com.cinema.movie_service.domain.ScreeningRepositoryPort;
import com.cinema.movie_service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScreeningService {

    private final ScreeningRepositoryPort repositoryPort;
    private final MovieService movieService; // valida que la película exista y aporta el título

    public ScreeningView create(UUID movieId, String room, LocalDateTime showAt, int totalSeats) {
        Movie movie = movieService.getById(movieId);
        Screening saved = repositoryPort.save(Screening.open(movieId, room, showAt, totalSeats));
        return new ScreeningView(saved, movie.getTitle());
    }

    public ScreeningView getById(UUID id) {
        Screening screening = repositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screening not found with id: " + id));
        return new ScreeningView(screening, movieService.getById(screening.getMovieId()).getTitle());
    }

    public List<ScreeningView> getAll() {
        Map<UUID, String> titles = movieService.getAll().stream()
                .collect(Collectors.toMap(Movie::getId, Movie::getTitle));
        return repositoryPort.findAll().stream()
                .map(s -> new ScreeningView(s, titles.getOrDefault(s.getMovieId(), "")))
                .toList();
    }

    /** Paso 3 del SAGA de reserva. Bloqueo pesimista: dos reservas simultáneas no pueden pisarse. */
    @Transactional
    public ScreeningView reserveSeat(UUID id) {
        Screening screening = findForUpdate(id);
        screening.reserve(); // invariante en la entidad
        return toView(repositoryPort.save(screening));
    }

    /** Compensación del SAGA (o cancelación de una reserva). */
    @Transactional
    public ScreeningView releaseSeat(UUID id) {
        Screening screening = findForUpdate(id);
        screening.release();
        return toView(repositoryPort.save(screening));
    }

    private Screening findForUpdate(UUID id) {
        return repositoryPort.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screening not found with id: " + id));
    }

    private ScreeningView toView(Screening screening) {
        return new ScreeningView(screening, movieService.getById(screening.getMovieId()).getTitle());
    }
}
