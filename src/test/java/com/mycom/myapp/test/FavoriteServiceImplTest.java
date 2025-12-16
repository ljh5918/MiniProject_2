package com.mycom.myapp.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycom.myapp.favorite.entity.Favorite;
import com.mycom.myapp.favorite.repository.FavoriteRepository;
import com.mycom.myapp.favorite.service.FavoriteServiceImpl;
import com.mycom.myapp.movie.entity.Movie;
import com.mycom.myapp.movie.repository.MovieRepository;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceImplTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private MovieRepository movieRepository;

<<<<<<< Updated upstream
    @Mock
    private UserRepository userRepository;
=======
    // ⚠️ 반드시 실 DB에 존재하는 사용자 이메일
    private final String EXIST_EMAIL = "asd@asd.com";
>>>>>>> Stashed changes

    @InjectMocks
    private FavoriteServiceImpl favoriteService;

    // 테스트에 사용할 공통 엔티티
    private User testUser;
    private Movie testMovie;
    private final String TEST_EMAIL = "test@user.com";
    private final Long TEST_MOVIE_ID = 100L;
    private final String TEST_TITLE = "Test Movie";
    private final String TEST_POSTER = "/test/poster.jpg";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .email(TEST_EMAIL)
                .nickname("Tester")
                .build();

        testMovie = Movie.builder()
                .movieId(TEST_MOVIE_ID)
                .title(TEST_TITLE)
                .posterPath(TEST_POSTER)
                .build();

        // 공통 Mocking: 모든 테스트에서 사용자 찾기는 성공한다고 가정
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(testUser));
    }

    // ---------------------- Add Favorite Tests ----------------------

    @Test
    @Order(1)
    @DisplayName("찜하기 성공: Movie가 DB에 없을 경우 (Insert 후 찜)")
    void addFavorite_success_movie_not_exist() {
        // given
        // 1. Movie가 DB에 없음 -> Optional.empty() 반환
        given(movieRepository.findById(TEST_MOVIE_ID)).willReturn(Optional.empty());
        // 2. Movie를 저장하면 (save) 저장된 Movie 객체 반환
        given(movieRepository.save(any(Movie.class))).willReturn(testMovie);
        // 3. 찜 정보가 중복되지 않음
        given(favoriteRepository.existsByUserAndMovie(testUser, testMovie)).willReturn(false);

        // when
        favoriteService.addFavorite(TEST_EMAIL, TEST_MOVIE_ID, TEST_TITLE, TEST_POSTER);

        // then
        // 1. Movie 저장 로직이 호출되었는지 확인
        verify(movieRepository).save(any(Movie.class));
        // 2. Favorite 저장 로직이 호출되었는지 확인
        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    @Order(2)
    @DisplayName("찜하기 성공: Movie가 DB에 이미 있을 경우 (Update 없이 찜)")
    void addFavorite_success_movie_already_exist() {
        // given
        // 1. Movie가 DB에 이미 존재함 -> Optional.of(testMovie) 반환
        given(movieRepository.findById(TEST_MOVIE_ID)).willReturn(Optional.of(testMovie));
        // 2. 찜 정보가 중복되지 않음
        given(favoriteRepository.existsByUserAndMovie(testUser, testMovie)).willReturn(false);

        // when
        favoriteService.addFavorite(TEST_EMAIL, TEST_MOVIE_ID, TEST_TITLE, TEST_POSTER);

        // then
        // 1. Movie 저장 로직은 호출되지 않아야 함
        verify(movieRepository, never()).save(any(Movie.class));
        // 2. Favorite 저장 로직은 호출되어야 함
        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    @Order(3)
    @DisplayName("찜하기 실패: 이미 찜한 정보일 경우 (중복 저장 방지)")
    void addFavorite_fail_duplicate() {
        // given
        given(movieRepository.findById(TEST_MOVIE_ID)).willReturn(Optional.of(testMovie));
        // 1. 찜 정보가 이미 존재함
        given(favoriteRepository.existsByUserAndMovie(testUser, testMovie)).willReturn(true);

        // when
        favoriteService.addFavorite(TEST_EMAIL, TEST_MOVIE_ID, TEST_TITLE, TEST_POSTER);

        // then
        // 1. Favorite 저장 로직은 호출되지 않아야 함
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    // ---------------------- Get Favorites Tests ----------------------

    @Test
    @Order(4)
    @DisplayName("찜 목록 조회 성공")
    void getFavorites_success() {
        // given
        Movie movie1 = Movie.builder().movieId(200L).title("Movie A").build();
        Movie movie2 = Movie.builder().movieId(300L).title("Movie B").build();

        Favorite fav1 = Favorite.builder().movie(movie1).user(testUser).build();
        Favorite fav2 = Favorite.builder().movie(movie2).user(testUser).build();

        List<Favorite> favorites = Arrays.asList(fav1, fav2);

        // favoriteRepository.findByUserWithMovie 호출 시 Mock 데이터 반환
        given(favoriteRepository.findByUserWithMovie(testUser)).willReturn(favorites);

        // when
        List<Movie> result = favoriteService.getFavorites(TEST_EMAIL);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Movie A");
        assertThat(result.get(1).getTitle()).isEqualTo("Movie B");
    }

    @Test
    @Order(5)
    @DisplayName("찜 목록 조회 실패: User not found")
    void getFavorites_fail_user_not_found() {
        // given: setup에서 User 찾기는 성공시키지만, 이 테스트에서는 실패하도록 재정의
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> favoriteService.getFavorites(TEST_EMAIL))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    // ---------------------- Delete Favorite Tests ----------------------

    @Test
    @Order(6)
    @DisplayName("찜 삭제 성공")
    void deleteFavorite_success() {
        // given
        Favorite existingFavorite = Favorite.builder().user(testUser).movie(testMovie).build();

        // 1. Movie 존재
        given(movieRepository.findById(TEST_MOVIE_ID)).willReturn(Optional.of(testMovie));
        // 2. Favorite 존재
        given(favoriteRepository.findByUserAndMovie(testUser, testMovie)).willReturn(Optional.of(existingFavorite));

        // when
        favoriteService.deleteFavorite(TEST_EMAIL, TEST_MOVIE_ID);

        // then
        // delete 메소드가 호출되었는지 검증
        verify(favoriteRepository).delete(existingFavorite);
    }

    @Test
    @Order(7)
    @DisplayName("찜 삭제 실패: Movie not found")
    void deleteFavorite_fail_movie_not_found() {
        // given
        // Movie가 DB에 없음
        given(movieRepository.findById(TEST_MOVIE_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> favoriteService.deleteFavorite(TEST_EMAIL, TEST_MOVIE_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Movie not found");
        
        // delete는 호출되지 않아야 함
        verify(favoriteRepository, never()).delete(any(Favorite.class));
    }

    @Test
    @Order(8)
    @DisplayName("찜 삭제 실패: 찜 정보가 존재하지 않음")
    void deleteFavorite_fail_favorite_not_found() {
        // given
        // 1. Movie 존재
        given(movieRepository.findById(TEST_MOVIE_ID)).willReturn(Optional.of(testMovie));
        // 2. Favorite 정보 없음
        given(favoriteRepository.findByUserAndMovie(testUser, testMovie)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> favoriteService.deleteFavorite(TEST_EMAIL, TEST_MOVIE_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("찜 정보가 존재하지 않습니다.");

        // delete는 호출되지 않아야 함
        verify(favoriteRepository, never()).delete(any(Favorite.class));
    }
}