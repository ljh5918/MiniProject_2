package com.mycom.myapp.movie.repository;

import com.mycom.myapp.movie.entity.PopularMovieHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PopularMovieHistoryRepository extends JpaRepository<PopularMovieHistory, Long> {


    @Query(value = "SELECT p FROM PopularMovieHistory p WHERE p.cachedAt = " +
                   "(SELECT MAX(p2.cachedAt) FROM PopularMovieHistory p2) ORDER BY p.rankId ASC")
    List<PopularMovieHistory> findLatestPopularMovies();

    List<PopularMovieHistory> findByCachedAtBetweenOrderByCachedAtDescRankIdAsc(LocalDateTime start, LocalDateTime end);
}