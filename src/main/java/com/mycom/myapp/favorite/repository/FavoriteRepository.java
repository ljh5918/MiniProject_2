package com.mycom.myapp.favorite.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mycom.myapp.favorite.entity.Favorite;
import com.mycom.myapp.movie.entity.Movie;
import com.mycom.myapp.user.entity.User;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserAndMovie(User user, Movie movie);

    @Query("SELECT f FROM Favorite f JOIN FETCH f.movie WHERE f.user = :user")
    List<Favorite> findByUserWithMovie(@Param("user") User user);

    Optional<Favorite> findByUserAndMovie(User user, Movie movie);  // 삭제용
    
    
    // test 코드 위한 메서드 추가
    boolean existsByUser_EmailAndMovie_MovieId(String email, Long movieId);

}
