package com.mycom.myapp.movie.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbMovieDto {
    
    private Long id;
    private String title;          // TMDB가 주는 제목 (한글 or 원어)
    
    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("overview")
    private String overview;
    
    //영화 예고편 추가
    private String videoKey;
    // ▼ [추가된 부분] 필터링을 위해 필요한 정보
    @JsonProperty("original_title")
    private String originalTitle;    // 원제 (예: Iron Man)

    @JsonProperty("original_language")
    private String originalLanguage; // 언어 코드 (예: en, ko, ja...)
}