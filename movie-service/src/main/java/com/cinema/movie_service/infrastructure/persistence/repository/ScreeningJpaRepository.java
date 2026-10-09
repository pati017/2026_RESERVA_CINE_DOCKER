package com.cinema.movie_service.infrastructure.persistence.repository;

import com.cinema.movie_service.infrastructure.persistence.entity.ScreeningEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScreeningJpaRepository extends JpaRepository<ScreeningEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ScreeningEntity s where s.id = :id")
    Optional<ScreeningEntity> findByIdForUpdate(@Param("id") UUID id);
}
