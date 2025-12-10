package com.mycom.myapp.favorite.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.favorite.entity.Favorite;
import com.mycom.myapp.favorite.repository.FavoriteRepository;
import com.mycom.myapp.movie.entity.Movie;
import com.mycom.myapp.movie.repository.MovieRepository;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void addFavorite(String email, Long movieId, String title, String posterPath) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Movie 테이블에 없으면 Insert (캐싱용)
        Movie movie = movieRepository.findById(movieId).orElseGet(() ->
            movieRepository.save(
                Movie.builder()
                        .movieId(movieId)
                        .title(title)
                        .posterPath(posterPath)
                        .build()
            )
        );

        // 중복 저장 방지
        if (favoriteRepository.existsByUserAndMovie(user, movie)) {
            return;
        }

        favoriteRepository.save(
                Favorite.builder()
                        .user(user)
                        .movie(movie)
                        .build()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Movie> getFavorites(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return favoriteRepository.findByUserWithMovie(user)
                .stream()
                .map(Favorite::getMovie)
                .collect(Collectors.toList());
    }
    
    
    @Override
    @Transactional
    public void deleteFavorite(String email, Long movieId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Favorite favorite = favoriteRepository.findByUserAndMovie(
                user,
                movieRepository.findById(movieId).orElseThrow(() -> new RuntimeException("Movie not found"))
        ).orElseThrow(() -> new RuntimeException("찜 정보가 존재하지 않습니다."));

        favoriteRepository.delete(favorite);
    }
   
}
