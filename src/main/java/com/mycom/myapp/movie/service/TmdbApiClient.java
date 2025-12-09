package com.mycom.myapp.movie.service;

import com.mycom.myapp.movie.dto.TmdbMovieDto;
import com.mycom.myapp.movie.dto.TmdbResponseDto; // 위에서 만든 클래스 import
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TmdbApiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    // application.yml (또는 .env)에 있는 키를 가져옴
    @Value("${TMDB_API_KEY}") 
    private String apiKey;

    private static final String BASE_URL = "https://api.themoviedb.org/3";

    // 인기 영화 목록
    public List<TmdbMovieDto> getPopularMovies() {
        // URL 만들기
        String url = BASE_URL + "/movie/popular?api_key=" + apiKey + "&language=ko-KR&page=1";
        
        // 호출! (ResponseDto로 받아서 results만 꺼냄)
        TmdbResponseDto response = restTemplate.getForObject(url, TmdbResponseDto.class);
        
        return response != null ? response.getResults() : List.of();
    }
    
    // 영화 검색 (Search)
    public List<TmdbMovieDto> searchMovies(String query) {
        String url = BASE_URL + "/search/movie?api_key=" + apiKey + "&language=ko-KR&query=" + query;
        TmdbResponseDto response = restTemplate.getForObject(url, TmdbResponseDto.class);
        return response != null ? response.getResults() : List.of();
    }

    // 영화 상세 정보 (Detail) - 리스트가 아니라 단일 객체 반환
    public TmdbMovieDto getMovieDetail(Long tmdbId) {
        String url = BASE_URL + "/movie/" + tmdbId + "?api_key=" + apiKey + "&language=ko-KR";
        // 얘는 목록(TmdbResponseDto)이 아니라 영화(TmdbMovieDto) 자체를 받습니다.
        return restTemplate.getForObject(url, TmdbMovieDto.class);
    }

    // 추천 영화 목록 (Recommendations)
    public List<TmdbMovieDto> getRecommendations(Long tmdbId) {
        String url = BASE_URL + "/movie/" + tmdbId + "/recommendations?api_key=" + apiKey + "&language=ko-KR";
        TmdbResponseDto response = restTemplate.getForObject(url, TmdbResponseDto.class);
        return response != null ? response.getResults() : List.of();
    }
}