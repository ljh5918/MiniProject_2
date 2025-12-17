package com.mycom.myapp.movie.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping; 
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.movie.dto.TmdbMovieDto;
import com.mycom.myapp.movie.entity.Movie;
import com.mycom.myapp.movie.service.MovieService; 

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/movies")
public class MovieController {

    private final MovieService movieService; 

 // 1. 인기 영화 (GET /movies/popular) - DB 캐시 사용
    @GetMapping("/popular")
    public List<TmdbMovieDto> getPopular(
            @RequestParam(name = "page", defaultValue = "1") int page
    ) {
        return movieService.getPopularMovies(page);
    }


    // 2. 검색 (GET /movies/search?q=...) - API 직접 호출
    @GetMapping("/search")
    public ResponseEntity<List<TmdbMovieDto>> searchMovies(
            @RequestParam("q") String query,
            @RequestParam(value = "useApi", defaultValue = "true") boolean useApi // ⭐ 추가
    ) {
        // 서비스에 useApi 값 전달
        return ResponseEntity.ok(movieService.searchMovies(query, useApi));
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
    
    @GetMapping("/list")
    public ResponseEntity<Page<TmdbMovieDto>> getAllMovies(
            @RequestParam(name = "page", defaultValue = "1") int page
    ) {
        return ResponseEntity.ok(movieService.getAllMovies(page));
    }

    
    //db에 저장된 영화 갯수 리턴
    @GetMapping("/count")	
    public ResponseEntity<Long> getMoviesCount() {
    	return ResponseEntity.ok(movieService.getMovieCount());
    	
    }
}











