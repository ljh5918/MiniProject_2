//package com.mycom.myapp.movie.service;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import com.mycom.myapp.movie.dto.TmdbMovieDto;
//import com.mycom.myapp.movie.dto.TmdbResponseDto;
//
//import lombok.Data;
//import lombok.RequiredArgsConstructor;
//
//@Service
//@RequiredArgsConstructor
//public class TmdbApiClient {
//
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    @Value("${TMDB_API_KEY}")
//    private String apiKey;
//
//    private static final String BASE_URL = "https://api.themoviedb.org/3";
//
//    // 1. 인기 영화
//    public List<TmdbMovieDto> getPopularMovies() {
//        String url = BASE_URL + "/movie/popular?api_key=" + apiKey + "&language=ko-KR&page=1";
//        TmdbResponseDto response = restTemplate.getForObject(url, TmdbResponseDto.class);
//        
//        // ▼ 필터링 적용해서 리턴
//        return filterMovies(response.getResults());
//    }
//
//    // 2. 검색
//    public List<TmdbMovieDto> searchMovies(String query) {
//        String url = BASE_URL + "/search/movie?api_key=" + apiKey + "&language=ko-KR&query=" + query;
//        TmdbResponseDto response = restTemplate.getForObject(url, TmdbResponseDto.class);
//        
//        // ▼ 필터링 적용해서 리턴
//        return filterMovies(response.getResults());
//    }
//
//    // 3. 상세 조회 (단건이라 필터링 대신 로직 직접 적용)
//    public TmdbMovieDto getMovieDetail(Long tmdbId) {
//        String url = BASE_URL + "/movie/" + tmdbId + "?api_key=" + apiKey + "&language=ko-KR";
//        return restTemplate.getForObject(url, TmdbMovieDto.class);
//    }
//
//    // 4. 추천 영화
//    public List<TmdbMovieDto> getRecommendations(Long tmdbId) {
//        String url = BASE_URL + "/movie/" + tmdbId + "/recommendations?api_key=" + apiKey + "&language=ko-KR";
//        TmdbResponseDto response = restTemplate.getForObject(url, TmdbResponseDto.class);
//        
//        // ▼ 필터링 적용해서 리턴
//        return filterMovies(response.getResults());
//    }
//
//    // ⭐ [핵심] 필터링 및 제목 수정 로직
//    private List<TmdbMovieDto> filterMovies(List<TmdbMovieDto> movies) {
//        if (movies == null) return List.of();
//
//        return movies.stream()
//            .filter(movie -> {
//                String title = movie.getTitle();
//                String orgLang = movie.getOriginalLanguage();
//                String orgTitle = movie.getOriginalTitle();
//
//                // 1. 제목에 한글이 한 글자라도 있으면 -> 통과 (OK)
//                if (title != null && title.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*")) {
//                    return true;
//                }
//
//                // 2. 한글이 없는데, 원어(Original Language)가 영어('en')라면 -> 통과 (OK)
//                //    단, 제목을 영어 원제로 바꿔준다. (꼬부랑 글씨 방지)
//                if ("en".equals(orgLang)) {
//                    movie.setTitle(orgTitle); // 제목을 영어로 교체
//                    return true;
//                }
//
//                // 3. 한글도 없고, 영어도 아니다(인도어, 일본어 등) -> 제외 (Drop)
//                return false; 
//            })
//            .collect(Collectors.toList());
//    }
//    
// // ... 기존 코드 ...
//
//    // ⭐ [추가] 영화 ID로 예고편 Key 가져오기
//    public String getMovieTrailerKey(Long movieId) {
//        String url = BASE_URL + "/movie/" + movieId + "/videos?api_key=" + apiKey + "&language=ko-KR";
//        
//        try {
//            // 1. 결과 받아오기
//            VideoResponse response = restTemplate.getForObject(url, VideoResponse.class);
//            
//            if (response != null && response.getResults() != null) {
//                // 2. YouTube이면서 Trailer인 것 찾기 (없으면 Teaser라도)
//                return response.getResults().stream()
//                        .filter(v -> "YouTube".equals(v.getSite()))
//                        .filter(v -> "Trailer".equals(v.getType()))
//                        .findFirst() // 첫 번째 것 발견하면 리턴
//                        .map(VideoResult::getKey)
//                        .orElse(null); // 없으면 null
//            }
//        } catch (Exception e) {
//            System.out.println("예고편 없음: " + movieId);
//        }
//        return null;
//    }
//
//    // 내부 클래스 (JSON 파싱용) - TmdbApiClient 클래스 맨 아래에 넣으세요
//    @Data
//    static class VideoResponse {
//        private List<VideoResult> results;
//    }
//
//    @Data
//    static class VideoResult {
//        private String key;
//        private String site;
//        private String type;
//    }
//}








package com.mycom.myapp.movie.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.mycom.myapp.movie.dto.TmdbMovieDto;
import com.mycom.myapp.movie.dto.TmdbResponseDto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TmdbApiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${TMDB_API_KEY}")
    private String apiKey;

    private static final String BASE_URL = "https://api.themoviedb.org/3";

    // 1. 인기 영화
    public List<TmdbMovieDto> getPopularMovies(int page) {
        String url = BASE_URL + "/movie/popular?api_key=" + apiKey
                   + "&language=ko-KR&page=" + page;

        TmdbResponseDto response = restTemplate.getForObject(url, TmdbResponseDto.class);
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
    
 // ... 기존 코드 ...

    // ⭐ [추가] 영화 ID로 예고편 Key 가져오기
    public String getMovieTrailerKey(Long movieId) {
        String url = BASE_URL + "/movie/" + movieId + "/videos?api_key=" + apiKey + "&language=ko-KR";
        
        try {
            // 1. 결과 받아오기
            VideoResponse response = restTemplate.getForObject(url, VideoResponse.class);
            
            if (response != null && response.getResults() != null) {
                // 2. YouTube이면서 Trailer인 것 찾기 (없으면 Teaser라도)
                return response.getResults().stream()
                        .filter(v -> "YouTube".equals(v.getSite()))
                        .filter(v -> "Trailer".equals(v.getType()))
                        .findFirst() // 첫 번째 것 발견하면 리턴
                        .map(VideoResult::getKey)
                        .orElse(null); // 없으면 null
            }
        } catch (Exception e) {
            System.out.println("예고편 없음: " + movieId);
        }
        return null;
    }

    // 내부 클래스 (JSON 파싱용) - TmdbApiClient 클래스 맨 아래에 넣으세요
    @Data
    static class VideoResponse {
        private List<VideoResult> results;
    }

    @Data
    static class VideoResult {
        private String key;
        private String site;
        private String type;
    }
}
