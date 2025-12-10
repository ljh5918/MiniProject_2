package com.mycom.myapp.movie.service;

import com.mycom.myapp.movie.dto.TmdbMovieDto;
import com.mycom.myapp.movie.entity.Movie;
import com.mycom.myapp.movie.entity.PopularMovieHistory;
import com.mycom.myapp.movie.repository.MovieRepository;
import com.mycom.myapp.movie.repository.PopularMovieHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {

    private final MovieRepository movieRepository;
    private final TmdbApiClient tmdbApiClient;
    private final PopularMovieHistoryRepository popularMovieHistoryRepository; 
    
    private static final int CACHE_TTL_HOURS = 24;

    // ------------------------------------------------------------------
    // A. [핵심 캐싱] Movie Entity 조회 또는 저장
    // ------------------------------------------------------------------

    @Transactional
    public Movie getOrSaveMovie(Long tmdbId) {
        Optional<Movie> movieOptional = movieRepository.findById(tmdbId);
        if (movieOptional.isPresent()) {
            return movieOptional.get();
        }

        log.info("Movie ID {} 정보가 DB에 없어 TMDB API를 호출하여 캐싱을 시도합니다.", tmdbId);
        
        try {
            TmdbMovieDto movieDto = tmdbApiClient.getMovieDetail(tmdbId);
            
            Movie newMovie = Movie.builder()
                    .movieId(movieDto.getId())
                    .title(movieDto.getTitle())
                    .posterPath(movieDto.getPosterPath())
                    .build();
            
            return movieRepository.save(newMovie);
            
        } catch (Exception e) {
            log.error("TMDB API 호출 및 캐싱 실패: Movie ID {}", tmdbId, e);
            throw new RuntimeException("영화 정보 캐싱에 실패했습니다.", e);
        }
    }

    // 인기 영화 조회
    // 최신 이력이 24시간 이내면 그대로 가져오고 아니면 API를 호출하여 신규 인기 영화 데이터 저장
 
    @Transactional
    public List<TmdbMovieDto> getPopularMovies() {
        
        List<PopularMovieHistory> historyList = popularMovieHistoryRepository.findLatestPopularMovies();
        
        LocalDateTime cacheThreshold = LocalDateTime.now().minusHours(CACHE_TTL_HOURS);
        
        if (!historyList.isEmpty()) {
            LocalDateTime latestCachedTime = historyList.get(0).getCachedAt();
            if (latestCachedTime.isAfter(cacheThreshold)) {
                log.info("인기 영화 캐시 유효함. {}시간 이내 데이터 존재. DB에서 반환.", CACHE_TTL_HOURS);
                return convertHistoryToDto(historyList);
            }
        }
        
        log.warn("최신 인기 영화 저장을 시작합니다... TMDB API 호출 및 DB에 신규 이력 저장중.");
        
        List<PopularMovieHistory> newHistoryList = saveNewPopularMovieHistory();
        return convertHistoryToDto(newHistoryList);
    }
    
    /**
     * TMDB API를 호출하고 PopularMovieHistory 테이블에 새로운 이력을 누적 저장합니다.
     */
    @Transactional
    private List<PopularMovieHistory> saveNewPopularMovieHistory() {
        List<TmdbMovieDto> popularMovies = tmdbApiClient.getPopularMovies();
        LocalDateTime now = LocalDateTime.now();
        int rank = 1;
        
        List<PopularMovieHistory> savedHistories = new java.util.ArrayList<>();

        for (TmdbMovieDto dto : popularMovies) {
            if (rank > 20) break; 

            try {
                Movie movie = getOrSaveMovie(dto.getId()); 
                PopularMovieHistory history = PopularMovieHistory.builder()
                        .cachedAt(now)
                        .rankId(rank)
                        .movie(movie)
                        .build();
                        
                popularMovieHistoryRepository.save(history);
                savedHistories.add(history);
                
            } catch (Exception e) {
                log.error("순위 {} 영화 누적 저장 중 오류 발생 (TMDB ID: {}): {}", rank, dto.getId(), e.getMessage());
            }
            rank++;
        }
        log.info("⭐ [JIT] 인기 영화 이력 저장 완료. 총 {}건 저장됨.", rank - 1);
        return savedHistories;
    }


    // History Entity 목록을 TmdbMovieDto 목록으로 변환하는 보조 메서드
    private List<TmdbMovieDto> convertHistoryToDto(List<PopularMovieHistory> historyList) {
        return historyList.stream()
                .map(history -> {
                    Movie movie = history.getMovie();
                    TmdbMovieDto dto = new TmdbMovieDto();
                    dto.setId(movie.getMovieId());
                    dto.setTitle(movie.getTitle());
                    dto.setPosterPath(movie.getPosterPath());
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    // api Controller으로 넘기는 로직
    public List<TmdbMovieDto> searchMovies(String query) {
        return tmdbApiClient.searchMovies(query);
    }
    public TmdbMovieDto getMovieDetail(Long tmdbId) {
        return tmdbApiClient.getMovieDetail(tmdbId);
    }
    public List<TmdbMovieDto> getRecommendations(Long tmdbId) {
        return tmdbApiClient.getRecommendations(tmdbId);
    }
}