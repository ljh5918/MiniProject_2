package com.mycom.myapp.movie.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping; 
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.movie.dto.TmdbMovieDto;
import com.mycom.myapp.movie.service.MovieService; 

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/movies")
public class MovieController {

    private final MovieService movieService; 

 // 1. 인기 영화 (GET /movies/popular) - DB 캐시 사용
    @GetMapping("/popular")
    public List<TmdbMovieDto> getPopular() {
        return movieService.getPopularMovies(); // ⭐ MovieService의 JIT 로직 호출
    }

    // 2. 검색 (GET /movies/search?q=...) - API 직접 호출
    @GetMapping("/search")
    public List<TmdbMovieDto> searchMovies(@RequestParam("q") String query) {
        return movieService.searchMovies(query);
    }

    // 3. 상세 조회 (GET /movies/detail/{id}) - API 직접 호출
    @GetMapping("/detail/{id}")
    public TmdbMovieDto getDetail(@PathVariable("id") Long id) {
        return movieService.getMovieDetail(id);
    }

    // 4. 추천 영화 (GET /movies/recommend/{id}) - API 직접 호출
    @GetMapping("/recommend/{id}")
    public List<TmdbMovieDto> getRecommend(@PathVariable("id") Long id) {
        return movieService.getRecommendations(id);
    }
}