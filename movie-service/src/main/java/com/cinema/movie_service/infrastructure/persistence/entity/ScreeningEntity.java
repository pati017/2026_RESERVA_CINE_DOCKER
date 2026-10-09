package com.cinema.movie_service.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "screenings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "movie_id", nullable = false)
    private UUID movieId;

    @Column(nullable = false)
    private String room;

    @Column(name = "show_at", nullable = false)
    private LocalDateTime showAt;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Builder.Default
    private boolean active = true;
}
