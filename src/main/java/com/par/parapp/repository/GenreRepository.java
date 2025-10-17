package com.par.parapp.repository;

import com.par.parapp.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Long> {
    Optional<Genre> findByName(String name);

    @Query(value = "SELECT * FROM genres WHERE upper(name)=upper(:name) LIMIT 1", nativeQuery = true)
    Optional<Genre> findFirstByNameIgnoreCase(@Param("name") String name);
}
