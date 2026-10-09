package com.cinema.movie_service.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Agregado Movie (catálogo). Sin setters públicos: el estado solo cambia mediante
 * métodos de negocio, de modo que el dominio se protege a sí mismo.
 */
@Getter
@Builder
public class Movie {
    private UUID id;
    private String title;
    private String director;
    private Genre genre;
    private Integer durationMin;
    private String rating;
    @Builder.Default
    private boolean active = true;
    private Instant createdAt;
    private Instant updatedAt;

    public void update(String title, String director, Genre genre, Integer durationMin, String rating) {
        this.title = title;
        this.director = director;
        this.genre = genre;
        this.durationMin = durationMin;
        this.rating = rating;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}
