package com.mycom.myapp.favorite.service;

import java.util.List;
import com.mycom.myapp.movie.entity.Movie;

public interface FavoriteService {

    void addFavorite(String email, Long movieId, String title, String posterPath);

    List<Movie> getFavorites(String email);
    
    void deleteFavorite(String email, Long movieId);  // 삭제 메서드 추가

}
