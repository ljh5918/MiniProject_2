package com.mycom.myapp.test;

import com.mycom.myapp.comment.dto.CommentRequest;
import com.mycom.myapp.comment.dto.CommentResponse;
import com.mycom.myapp.comment.entity.Comment;
import com.mycom.myapp.comment.repository.CommentRepository;
import com.mycom.myapp.comment.service.CommentServiceImpl;
import com.mycom.myapp.movie.entity.Movie;
import com.mycom.myapp.movie.repository.MovieRepository;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

//    @Mock
    @Mock(lenient = true)
    private UserRepository userRepository;

//    @Mock
    //lenient = true를 추가하면, Mockito는 @BeforeEach나 특정 테스트에서 정의된 Mocking이 사용되지 않아도 예외를 발생시키지 않고 테스트를 계속 진행
    @Mock(lenient = true) 
    private MovieRepository movieRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    // 테스트에 사용할 공통 데이터
    private User testUser;
    private Movie testMovie;
    private Comment testComment;
    private final String TEST_EMAIL = "test@user.com";
    private final Long TEST_MOVIE_ID = 10L;
    private final Long TEST_COMMENT_ID = 1L;
    private final String INITIAL_CONTENT = "Original comment";
    private final String UPDATED_CONTENT = "Updated comment";

    @BeforeEach
    void setUp() {
        // 1. User Mock
        testUser = User.builder()
                .userId(1L)
                .email(TEST_EMAIL)
                .nickname("Tester")
                .build();

        // 2. Movie Mock
        testMovie = Movie.builder()
                .movieId(TEST_MOVIE_ID)
                .title("Test Movie")
                .build();
        
        // 3. Comment Mock
        testComment = Comment.builder()
                .commentId(TEST_COMMENT_ID)
                .user(testUser)
                .movie(testMovie)
                .content(INITIAL_CONTENT)
                .createdAt(LocalDateTime.now())
                .build();
        
        // ⭐⭐ [추가] 중요: 모든 테스트 전에 User 찾기를 성공으로 Mocking
        // 이 코드가 없어서 getCommentsByUser_success 테스트가 실패한 것입니다.
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(testUser));
    }

    // ---------------------- Add Comment Tests ----------------------

    @Test
    @DisplayName("댓글 추가 성공")
    void addComment_success() {
        // given
        CommentRequest req = new CommentRequest(TEST_MOVIE_ID, INITIAL_CONTENT);

        // Mocking: User, Movie 찾기 성공
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(testUser));
        given(movieRepository.findById(TEST_MOVIE_ID)).willReturn(Optional.of(testMovie));

        // Mocking: Comment 저장 성공
        // 저장 시 build된 Comment 객체를 받아서, ID가 붙은 testComment 객체를 반환한다고 가정
        given(commentRepository.save(any(Comment.class))).willReturn(testComment);

        // when
        CommentResponse result = commentService.addComment(TEST_EMAIL, req);

        // then
        assertThat(result.getCommentId()).isEqualTo(TEST_COMMENT_ID);
        assertThat(result.getContent()).isEqualTo(INITIAL_CONTENT);
        assertThat(result.getUserEmail()).isEqualTo(TEST_EMAIL);
        
        // save 메서드가 호출되었는지 확인
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 추가 실패: User not found")
    void addComment_fail_user_not_found() {
        // given
        CommentRequest req = new CommentRequest(TEST_MOVIE_ID, INITIAL_CONTENT);
        // Mocking: User 찾기 실패
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.addComment(TEST_EMAIL, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
        
        // save는 호출되지 않아야 함
        verify(commentRepository, never()).save(any());
    }
    
    // ---------------------- Get Comments (By Movie) Tests ----------------------

    @Test
    @DisplayName("영화별 댓글 목록 조회 성공")
    void getComments_success() {
        // given
        Comment c1 = Comment.builder().commentId(2L).user(testUser).movie(testMovie).content("Newest").createdAt(LocalDateTime.now().plusSeconds(1)).build();
        Comment c2 = Comment.builder().commentId(3L).user(testUser).movie(testMovie).content("Oldest").createdAt(LocalDateTime.now()).build();
        
        // Mocking: Movie 찾기 성공
        given(movieRepository.findById(TEST_MOVIE_ID)).willReturn(Optional.of(testMovie));
        // Mocking: 댓글 목록 조회 (OrderByCreatedAtDesc를 반영하여 c1이 먼저 오도록 설정)
        given(commentRepository.findByMovieOrderByCreatedAtDesc(testMovie)).willReturn(List.of(c1, c2));

        // when
        List<CommentResponse> result = commentService.getComments(TEST_MOVIE_ID);

        // then
        assertThat(result).hasSize(2);
        // 최신순 정렬 확인
        assertThat(result.get(0).getContent()).isEqualTo("Newest");
        assertThat(result.get(1).getContent()).isEqualTo("Oldest");
    }

    @Test
    @DisplayName("영화별 댓글 목록 조회 실패: Movie not found")
    void getComments_fail_movie_not_found() {
        // given
        // Mocking: Movie 찾기 실패
        given(movieRepository.findById(TEST_MOVIE_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.getComments(TEST_MOVIE_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Movie not found");
    }

    // ---------------------- Update Comment Tests ----------------------

    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment_success() {
        // given
        // Mocking: Comment 찾기 성공
        given(commentRepository.findById(TEST_COMMENT_ID)).willReturn(Optional.of(testComment));
        // Mocking: Comment 저장 (수정된 Comment 반환)
        given(commentRepository.save(any(Comment.class))).willAnswer(invocation -> {
            Comment savedComment = invocation.getArgument(0);
            return savedComment; // setContent가 적용된 객체를 반환
        });

        // when
        CommentResponse result = commentService.updateComment(TEST_EMAIL, TEST_COMMENT_ID, UPDATED_CONTENT);

        // then
        assertThat(result.getContent()).isEqualTo(UPDATED_CONTENT);
        // save 메서드가 호출되었는지 확인
        verify(commentRepository).save(any(Comment.class));
    }
    
    @Test
    @DisplayName("댓글 수정 실패: 작성자가 아닌 경우 (Forbidden)")
    void updateComment_fail_forbidden() {
        // given
        // Mocking: Comment 찾기 성공
        given(commentRepository.findById(TEST_COMMENT_ID)).willReturn(Optional.of(testComment));
        String wrongEmail = "not_author@test.com";

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(wrongEmail, TEST_COMMENT_ID, UPDATED_CONTENT))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Forbidden");

        // save는 호출되지 않아야 함
        verify(commentRepository, never()).save(any());
    }

    // ---------------------- Delete Comment Tests ----------------------

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteComment_success() {
        // given
        // Mocking: Comment 찾기 성공
        given(commentRepository.findById(TEST_COMMENT_ID)).willReturn(Optional.of(testComment));

        // when
        commentService.deleteComment(TEST_EMAIL, TEST_COMMENT_ID);

        // then
        // delete 메서드가 호출되었는지 검증
        verify(commentRepository).delete(testComment);
    }

    @Test
    @DisplayName("댓글 삭제 실패: 작성자가 아닌 경우 (Forbidden)")
    void deleteComment_fail_forbidden() {
        // given
        // Mocking: Comment 찾기 성공
        given(commentRepository.findById(TEST_COMMENT_ID)).willReturn(Optional.of(testComment));
        String wrongEmail = "not_author@test.com";

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(wrongEmail, TEST_COMMENT_ID))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Forbidden");

        // delete는 호출되지 않아야 함
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    // ---------------------- Get Comments By User Tests ----------------------

    @Test
    @DisplayName("사용자별 댓글 목록 조회 성공")
    void getCommentsByUser_success() {
        // given
        Comment c1 = Comment.builder().commentId(2L).user(testUser).movie(testMovie).content("User Comment A").createdAt(LocalDateTime.now().plusSeconds(1)).build();
        Comment c2 = Comment.builder().commentId(3L).user(testUser).movie(testMovie).content("User Comment B").createdAt(LocalDateTime.now()).build();

        // Mocking: User 찾기 성공 (BeforeEach에서 설정됨)
        // Mocking: 댓글 목록 조회 (OrderByCreatedAtDesc를 반영하여 c1이 먼저 오도록 설정)
        given(commentRepository.findByUserOrderByCreatedAtDesc(testUser)).willReturn(List.of(c1, c2));

        // when
        List<CommentResponse> result = commentService.getCommentsByUser(TEST_EMAIL);

        // then
        assertThat(result).hasSize(2);
        // 최신순 정렬 확인
        assertThat(result.get(0).getContent()).isEqualTo("User Comment A");
        assertThat(result.get(1).getContent()).isEqualTo("User Comment B");
    }

    @Test
    @DisplayName("사용자별 댓글 목록 조회 실패: User not found")
    void getCommentsByUser_fail_user_not_found() {
        // given
        // Mocking: User 찾기 실패
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.getCommentsByUser(TEST_EMAIL))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }
}