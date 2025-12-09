package com.mycom.myapp.movie.service;

import com.mycom.myapp.movie.dto.TmdbMovieDto;
import com.mycom.myapp.movie.dto.TmdbResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TmdbApiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${TMDB_API_KEY}")
    private String apiKey;

    private static final String BASE_URL = "https://api.themoviedb.org/3";

    // 1. 인기 영화
    public List<TmdbMovieDto> getPopularMovies() {
        String url = BASE_URL + "/movie/popular?api_key=" + apiKey + "&language=ko-KR&page=1";
        TmdbResponseDto response = restTemplate.getForObject(url, TmdbResponseDto.class);
        
        // ▼ 필터링 적용해서 리턴
        return filterMovies(response.getResults());
    }

    // 2. 검색
    public List<TmdbMovieDto> searchMovies(String query) {
        String url = BASE_URL + "/search/movie?api_key=" + apiKey + "&language=ko-KR&query=" + query;
        TmdbResponseDto response = restTemplate.getForObject(url, TmdbResponseDto.class);
        
        // ▼ 필터링 적용해서 리턴
        return filterMovies(response.getResults());
    }

    // 3. 상세 조회 (단건이라 필터링 대신 로직 직접 적용)
    public TmdbMovieDto getMovieDetail(Long tmdbId) {
        String url = BASE_URL + "/movie/" + tmdbId + "?api_key=" + apiKey + "&language=ko-KR";
        return restTemplate.getForObject(url, TmdbMovieDto.class);
    }

    // 4. 추천 영화
    public List<TmdbMovieDto> getRecommendations(Long tmdbId) {
        String url = BASE_URL + "/movie/" + tmdbId + "/recommendations?api_key=" + apiKey + "&language=ko-KR";
        TmdbResponseDto response = restTemplate.getForObject(url, TmdbResponseDto.class);
        
        // ▼ 필터링 적용해서 리턴
        return filterMovies(response.getResults());
    }

    // ⭐ [핵심] 필터링 및 제목 수정 로직
    private List<TmdbMovieDto> filterMovies(List<TmdbMovieDto> movies) {
        if (movies == null) return List.of();

        return movies.stream()
            .filter(movie -> {
                String title = movie.getTitle();
                String orgLang = movie.getOriginalLanguage();
                String orgTitle = movie.getOriginalTitle();

                // 1. 제목에 한글이 한 글자라도 있으면 -> 통과 (OK)
                if (title != null && title.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*")) {
                    return true;
                }

                // 2. 한글이 없는데, 원어(Original Language)가 영어('en')라면 -> 통과 (OK)
                //    단, 제목을 영어 원제로 바꿔준다. (꼬부랑 글씨 방지)
                if ("en".equals(orgLang)) {
                    movie.setTitle(orgTitle); // 제목을 영어로 교체
                    return true;
                }

                // 3. 한글도 없고, 영어도 아니다(인도어, 일본어 등) -> 제외 (Drop)
                return false; 
            })
            .collect(Collectors.toList());
    }
}