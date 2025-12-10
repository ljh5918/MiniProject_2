package com.mycom.myapp.favorite.entity;

import com.mycom.myapp.movie.entity.Movie;
import com.mycom.myapp.user.entity.User;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "favorite",
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_fav", columnNames = {"user_id", "movie_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long favoriteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;
}
