package com.mycom.myapp.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.movie.entity.Movie;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}
