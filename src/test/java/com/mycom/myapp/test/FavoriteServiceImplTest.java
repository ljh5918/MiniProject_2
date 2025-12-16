package com.mycom.myapp.test;

import com.mycom.myapp.favorite.repository.FavoriteRepository;
import com.mycom.myapp.favorite.service.FavoriteService;
import com.mycom.myapp.movie.entity.Movie;
import com.mycom.myapp.movie.repository.MovieRepository;
import com.mycom.myapp.user.repository.UserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class FavoriteServiceImplTest {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MovieRepository movieRepository;

    // ⚠️ 반드시 실 DB에 존재하는 사용자 이메일
    private final String EXIST_EMAIL = "asd@asd.com";

    // 테스트용 영화 정보 (DB에 없어도 됨)
    private final Long MOVIE_ID = 999999L;
    private final String TITLE = "TEST MOVIE";
    private final String POSTER = "/test.jpg";


    @Test
    @DisplayName("실 DB 기준: 찜 추가 성공")
    void add_favorite_success() {

        // given
        assertThat(userRepository.findByEmail(EXIST_EMAIL)).isPresent();

        // 혹시 남아 있을 데이터 제거 (테스트 안정성)
        if (favoriteRepository.existsByUser_EmailAndMovie_MovieId(EXIST_EMAIL, MOVIE_ID)) {
            favoriteService.deleteFavorite(EXIST_EMAIL, MOVIE_ID);
        }

        // when
        favoriteService.addFavorite(EXIST_EMAIL, MOVIE_ID, TITLE, POSTER);

        // then ✅ Lazy 접근 없음
        assertThat(
            favoriteRepository.existsByUser_EmailAndMovie_MovieId(EXIST_EMAIL, MOVIE_ID)
        ).isTrue();
    }

    @Test
    @DisplayName("실 DB 기준: 찜 목록 조회")
    void get_favorites_success() {

        // given (찜이 하나는 존재하도록 보장)
        favoriteService.addFavorite(EXIST_EMAIL, MOVIE_ID, TITLE, POSTER);

        // when
        List<Movie> favorites = favoriteService.getFavorites(EXIST_EMAIL);

        // then
        assertThat(favorites).isNotEmpty();
        assertThat(favorites)
                .anyMatch(movie -> movie.getMovieId().equals(MOVIE_ID));
    }

    
    
    @Test
    @DisplayName("실 DB 기준: 찜 삭제 성공")
    void delete_favorite_success() {

        // given
        favoriteService.addFavorite(EXIST_EMAIL, MOVIE_ID, TITLE, POSTER);

        // 사전 확인
        assertThat(
            favoriteRepository.existsByUser_EmailAndMovie_MovieId(EXIST_EMAIL, MOVIE_ID)
        ).isTrue();

        // when
        favoriteService.deleteFavorite(EXIST_EMAIL, MOVIE_ID);

        // then ✅ Lazy 문제 없음
        assertThat(
            favoriteRepository.existsByUser_EmailAndMovie_MovieId(EXIST_EMAIL, MOVIE_ID)
        ).isFalse();
    }


    @Test
    @DisplayName("실 DB 기준: 존재하지 않는 사용자 찜 조회 실패")
    void get_favorites_fail_user_not_found() {

        String notExistEmail = "ghost_user@test.com";

        assertThatThrownBy(() -> favoriteService.getFavorites(notExistEmail))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("실 DB 기준: 존재하지 않는 찜 삭제 실패")
    void delete_favorite_fail_not_exists() {

        Long notExistMovieId = 123123123L;

        assertThatThrownBy(() ->
                favoriteService.deleteFavorite(EXIST_EMAIL, notExistMovieId)
        ).isInstanceOf(RuntimeException.class);
    }
}
