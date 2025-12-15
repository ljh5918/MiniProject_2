package com.mycom.myapp.movie.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.movie.entity.Movie;

public interface MovieRepository extends JpaRepository<Movie, Long> {
	// 1. 중복 저장 방지용 (필드명 movieId와 일치시킴!)
    boolean existsByMovieId(Long movieId);

    // 2. 제목으로 DB 검색
    List<Movie> findByTitleContaining(String title);
}
