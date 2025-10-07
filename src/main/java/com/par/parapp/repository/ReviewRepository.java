package com.par.parapp.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.par.parapp.model.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query(value = "SELECT * FROM reviews WHERE reviews.game_id IN (SELECT games.id FROM games WHERE games.name=:gameName)", nativeQuery = true)
    Optional<Page<Review>> findByGameName(@Param("gameName") String gameName, Pageable pageable);
}
