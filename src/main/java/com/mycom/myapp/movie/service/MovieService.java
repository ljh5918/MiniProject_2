package com.mycom.myapp.movie.service;

import com.mycom.myapp.movie.dto.TmdbMovieDto;
import com.mycom.myapp.movie.entity.Movie;
import com.mycom.myapp.movie.repository.MovieRepository;
import com.mycom.myapp.movie.repository.PopularMovieHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Console;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {

    private final MovieRepository movieRepository;
    private final TmdbApiClient tmdbApiClient;
    private final PopularMovieHistoryRepository popularMovieHistoryRepository;

    private boolean useApi = true;

 // 1. 검색 (API 모드 / DB 모드 분기 처리)
    @Transactional
    public List<TmdbMovieDto> searchMovies(String query, boolean useApi) { // ⭐ 파라미터 추가

        // ⭐ useApi가 true일 때만 "수집(API 호출 + 저장)" 실행
        if (useApi) {
            List<TmdbMovieDto> apiResults = tmdbApiClient.searchMovies(query);
            for (TmdbMovieDto dto : apiResults) {
                if (!movieRepository.existsByMovieId(dto.getId())) {
                    Movie movie = Movie.builder()
                            .movieId(dto.getId())
                            .title(dto.getTitle())
                            .posterPath(dto.getPosterPath())
                            .build();
                    movieRepository.save(movie);
                }
            }
        }

        // ⭐ DB 조회는 모드 상관없이 무조건 실행 (결과 반환용)
        List<Movie> entities = movieRepository.findByTitleContaining(query);
        
        return entities.stream().map(entity -> {
            TmdbMovieDto dto = new TmdbMovieDto();
            dto.setId(entity.getMovieId());       
            dto.setTitle(entity.getTitle());
            dto.setPosterPath(entity.getPosterPath());
            return dto;
        }).collect(Collectors.toList());
    }

    // 2. 상세 조회 -> 무조건 API 호출 (db에 세부정보가없음)
    @Transactional
    public TmdbMovieDto getMovieDetail(Long tmdbId) {
        // API에서 상세 정보 가져오기
        TmdbMovieDto movieDto = tmdbApiClient.getMovieDetail(tmdbId);

        // db에 없는 영화 입력시 db에 영화 업데이트
        if (!movieRepository.existsByMovieId(tmdbId)) {
            Movie movie = Movie.builder()
                    .movieId(movieDto.getId())
                    .title(movieDto.getTitle())
                    .posterPath(movieDto.getPosterPath())
                    .build();
            movieRepository.save(movie);
        }
        String videoKey = tmdbApiClient.getMovieTrailerKey(tmdbId);
        movieDto.setVideoKey(videoKey);

        return movieDto;
    }
    @Transactional(readOnly = true)
    public Page<TmdbMovieDto> getAllMovies(int page) {
        
        // 1. 페이지 요청 생성 (0페이지부터 시작하므로 -1)
        // 정렬: 최신 저장된(ID가 큰) 순서대로
        Pageable pageable = PageRequest.of(page - 1, 20, Sort.by("movieId").descending());
        
        // 2. DB 전체 조회
        Page<Movie> moviePage = movieRepository.findAll(pageable);
        
        // 3. DTO 변환 (movieId -> id 매핑 필수!)
        return moviePage.map(entity -> {
            TmdbMovieDto dto = new TmdbMovieDto();
            dto.setId(entity.getMovieId());       // 프론트 호환성 유지
            dto.setTitle(entity.getTitle());
            dto.setPosterPath(entity.getPosterPath());
            return dto;
        });
    }
    
    // 인기영화 api
    @Cacheable("popularMovies") //spring cash 사용으로 360~500ms -> 5ms 까지 축소가능
    public List<TmdbMovieDto> getPopularMovies() {
        return tmdbApiClient.getPopularMovies(); 
    }
    
    // 추천영화 api
    public List<TmdbMovieDto> getRecommendations(Long tmdbId) {
        return tmdbApiClient.getRecommendations(tmdbId);
    }
    
    public Long getMovieCount() {
        return movieRepository.count(); 
    }
    
}