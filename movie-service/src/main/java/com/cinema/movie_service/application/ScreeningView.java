package com.cinema.movie_service.application;

import com.cinema.movie_service.domain.Screening;

/** Función + título de la película (lo necesita el booking-service para su snapshot). */
public record ScreeningView(Screening screening, String movieTitle) {
}
