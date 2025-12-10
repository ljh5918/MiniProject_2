//package com.mycom.myapp.favorite.repository;
//
//import java.util.List;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import com.mycom.myapp.favorite.entity.Favorite;
//import com.mycom.myapp.movie.entity.Movie;
//import com.mycom.myapp.user.entity.User;
//
//public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
//
//    boolean existsByUserAndMovie(User user, Movie movie);
//
//    List<Favorite> findByUser(User user);
//
//    
//
//}


package com.mycom.myapp.favorite.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mycom.myapp.favorite.entity.Favorite;
import com.mycom.myapp.movie.entity.Movie;
import com.mycom.myapp.user.entity.User;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserAndMovie(User user, Movie movie);

    // 기존 LAZY 문제를 해결하기 위해 fetch join
    @Query("SELECT f FROM Favorite f JOIN FETCH f.movie WHERE f.user = :user")
    List<Favorite> findByUserWithMovie(@Param("user") User user);
}
