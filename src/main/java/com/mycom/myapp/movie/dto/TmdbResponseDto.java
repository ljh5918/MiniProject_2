package com.mycom.myapp.movie.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbResponseDto {
    private List<TmdbMovieDto> results;
}