package com.mycom.myapp.movie.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.movie.dto.TmdbMovieDto;
import com.mycom.myapp.movie.service.TmdbApiClient;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MovieController {

    private final TmdbApiClient tmdbApiClient;

 // 1. 인기 영화
    @GetMapping("/test/popular")
    public List<TmdbMovieDto> testPopular() {
        return tmdbApiClient.getPopularMovies();
    }

    // 2. 검색 -> http://localhost:8080/test/search?q=아이언맨
    @GetMapping("/test/search")
    public List<TmdbMovieDto> testSearch(@RequestParam("q") String query) {
        return tmdbApiClient.searchMovies(query);
    }

    // 3. 상세 조회 -> http://localhost:8080/test/detail/550
    @GetMapping("/test/detail/{id}")
    public TmdbMovieDto testDetail(@PathVariable("id") Long id) {
        return tmdbApiClient.getMovieDetail(id);
    }

    // 4. 추천 영화  -> http://localhost:8080/test/recommend/550
    @GetMapping("/test/recommend/{id}")
    public List<TmdbMovieDto> testRecommend(@PathVariable("id") Long id) {
        return tmdbApiClient.getRecommendations(id);
    }
}