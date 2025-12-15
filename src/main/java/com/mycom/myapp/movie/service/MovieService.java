package com.mycom.myapp.movie.service;

import com.mycom.myapp.movie.dto.TmdbMovieDto;
import com.mycom.myapp.movie.entity.Movie;
import com.mycom.myapp.movie.repository.MovieRepository;
import com.mycom.myapp.movie.repository.PopularMovieHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // 1. 검색 
    @Transactional
    public List<TmdbMovieDto> searchMovies(String query) {
        
        // 1. API호출 후 DB에 없는 영화 데이터 채우기 
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
    public List<TmdbMovieDto> getPopularMovies() {
        return tmdbApiClient.getPopularMovies(); 
    }
    
    // 추천영화 api
    public List<TmdbMovieDto> getRecommendations(Long tmdbId) {
        return tmdbApiClient.getRecommendations(tmdbId);
    }
    
}