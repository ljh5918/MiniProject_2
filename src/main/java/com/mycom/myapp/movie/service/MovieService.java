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
    // 1. [수정됨] 상세 조회 (API 호출 -> DB 검사/저장 -> DTO 반환)
    // ------------------------------------------------------------------
    @Transactional
    public TmdbMovieDto getMovieDetail(Long tmdbId) {
        // 1. 화면에 보여줄 상세 정보(줄거리 등)는 무조건 API에서 최신으로 가져옵니다.
        TmdbMovieDto movieDto = tmdbApiClient.getMovieDetail(tmdbId);

        // 2. DB에 해당 영화가 존재하는지 확인합니다.
        // (API 호출은 이미 했으므로, DB에는 찜하기 등을 위한 최소 정보만 저장합니다)
        if (!movieRepository.existsById(tmdbId)) {
            log.info("상세 조회된 영화(ID: {})가 DB에 없어 기본 정보를 저장합니다.", tmdbId);
            
            Movie newMovie = Movie.builder()
                    .movieId(movieDto.getId())
                    .title(movieDto.getTitle())       // 제목
                    .posterPath(movieDto.getPosterPath()) // 포스터 경로
                    // ❌ 줄거리(Overview) 등 상세 정보는 DB 구조에 없으므로 저장하지 않습니다.
                    .build();
            
            movieRepository.save(newMovie);
        }

        // 3. API에서 가져온 상세 정보(DTO)를 그대로 반환합니다.
        return movieDto;
    }

    // ------------------------------------------------------------------
    // 2. [기존 유지] 인기 영화 로직 (JIT 캐싱)
    // ------------------------------------------------------------------
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
    
    @Transactional
    private List<PopularMovieHistory> saveNewPopularMovieHistory() {
        List<TmdbMovieDto> popularMovies = tmdbApiClient.getPopularMovies();
        LocalDateTime now = LocalDateTime.now();
        int rank = 1;
        
        List<PopularMovieHistory> savedHistories = new java.util.ArrayList<>();

        for (TmdbMovieDto dto : popularMovies) {
            if (rank > 20) break; 

            try {
                // 내부적으로 사용되는 엔티티 조회/저장 메소드 호출
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

    // ------------------------------------------------------------------
    // 3. [기존 유지] 내부 헬퍼 메소드 (Entity 조회/저장)
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
    
    // ------------------------------------------------------------------
    // 4. [기존 유지] 기타 Pass-through 메소드
    // ------------------------------------------------------------------
    public List<TmdbMovieDto> searchMovies(String query) {
        return tmdbApiClient.searchMovies(query);
    }

    public List<TmdbMovieDto> getRecommendations(Long tmdbId) {
        return tmdbApiClient.getRecommendations(tmdbId);
    }
}