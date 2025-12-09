package com.mycom.myapp.movie.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 필요한 거 외엔 무시
public class TmdbMovieDto {
    private Long id;
    private String title;
    
    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("overview")
    private String overview; // 줄거리도 가져와봅시다
}
