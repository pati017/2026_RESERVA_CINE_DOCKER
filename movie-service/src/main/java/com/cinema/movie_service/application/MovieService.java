package com.cinema.movie_service.application;

import com.cinema.movie_service.domain.Movie;
import com.cinema.movie_service.domain.MovieRepositoryPort;
import com.cinema.movie_service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepositoryPort repositoryPort;

    public Movie create(Movie movie) {
        return repositoryPort.save(movie);
    }

    public Movie getById(UUID id) {
        return repositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
    }

    public List<Movie> getAll() {
        return repositoryPort.findAll();
    }

    public Movie update(UUID id, Movie updateData) {
        Movie existing = getById(id);
        existing.update(updateData.getTitle(), updateData.getDirector(), updateData.getGenre(),
                updateData.getDurationMin(), updateData.getRating());
        return repositoryPort.save(existing);
    }

    public Movie activate(UUID id) {
        Movie existing = getById(id);
        existing.activate();
        return repositoryPort.save(existing);
    }

    public Movie deactivate(UUID id) {
        Movie existing = getById(id);
        existing.deactivate();
        return repositoryPort.save(existing);
    }

    public void delete(UUID id) {
        getById(id);
        repositoryPort.deleteById(id);
    }
}
