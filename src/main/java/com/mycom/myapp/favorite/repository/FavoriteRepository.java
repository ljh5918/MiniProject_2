package com.mycom.myapp.favorite.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.favorite.entity.Favorite;
import com.mycom.myapp.movie.entity.Movie;
import com.mycom.myapp.user.entity.User;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserAndMovie(User user, Movie movie);

    List<Favorite> findByUser(User user);
}
