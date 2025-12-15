package com.mycom.myapp.test;

import com.mycom.myapp.comment.dto.CommentRequest;
import com.mycom.myapp.comment.dto.CommentResponse;
import com.mycom.myapp.comment.repository.CommentRepository;
import com.mycom.myapp.comment.service.CommentService;
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
class CommentServiceImplTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MovieRepository movieRepository;

    // ⚠️ 실 DB에 반드시 존재해야 함
    private final String EXIST_EMAIL = "asd@asd.com";
    private final Long EXIST_MOVIE_ID = 1419406L; // 실제 movie 테이블에 존재하는 ID

    @Test
    @DisplayName("실 DB 기준: 댓글 작성 성공")
    void add_comment_success() {

        // 사전 검증
        assertThat(userRepository.findByEmail(EXIST_EMAIL)).isPresent();
        assertThat(movieRepository.findById(EXIST_MOVIE_ID)).isPresent();

        CommentRequest req = new CommentRequest();
        req.setMovieId(EXIST_MOVIE_ID);
        req.setContent("테스트 댓글입니다");

        // when
        CommentResponse res = commentService.addComment(EXIST_EMAIL, req);

        // then (DTO 기준 → Lazy 안전)
        assertThat(res.getCommentId()).isNotNull();
        assertThat(res.getContent()).isEqualTo("테스트 댓글입니다");
        assertThat(res.getUserEmail()).isEqualTo(EXIST_EMAIL);
    }

    @Test
    @DisplayName("실 DB 기준: 영화별 댓글 조회 성공")
    void get_comments_by_movie_success() {

        // given (댓글 하나는 존재하도록)
        CommentRequest req = new CommentRequest();
        req.setMovieId(EXIST_MOVIE_ID);
        req.setContent("조회용 댓글");

        commentService.addComment(EXIST_EMAIL, req);

        // when
        List<CommentResponse> comments = commentService.getComments(EXIST_MOVIE_ID);

        // then
        assertThat(comments).isNotEmpty();
        assertThat(comments)
                .anyMatch(c -> c.getContent().equals("조회용 댓글"));
    }

    @Test
    @DisplayName("실 DB 기준: 댓글 수정 성공")
    void update_comment_success() {

        // given
        CommentRequest req = new CommentRequest();
        req.setMovieId(EXIST_MOVIE_ID);
        req.setContent("수정 전");

        CommentResponse saved = commentService.addComment(EXIST_EMAIL, req);

        // when
        CommentResponse updated =
                commentService.updateComment(EXIST_EMAIL, saved.getCommentId(), "수정 후");

        // then
        assertThat(updated.getContent()).isEqualTo("수정 후");
    }

    @Test
    @DisplayName("실 DB 기준: 댓글 수정 실패 (작성자 아님)")
    void update_comment_fail_forbidden() {

        CommentRequest req = new CommentRequest();
        req.setMovieId(EXIST_MOVIE_ID);
        req.setContent("권한 테스트");

        CommentResponse saved = commentService.addComment(EXIST_EMAIL, req);

        assertThatThrownBy(() ->
                commentService.updateComment("other@test.com", saved.getCommentId(), "수정 시도")
        ).isInstanceOf(SecurityException.class);
    }

    @Test
    @DisplayName("실 DB 기준: 댓글 삭제 성공")
    void delete_comment_success() {

        // given
        CommentRequest req = new CommentRequest();
        req.setMovieId(EXIST_MOVIE_ID);
        req.setContent("삭제용 댓글");

        CommentResponse saved = commentService.addComment(EXIST_EMAIL, req);

        // when
        commentService.deleteComment(EXIST_EMAIL, saved.getCommentId());

        // then (exists 쿼리 → Lazy 없음)
        assertThat(commentRepository.existsById(saved.getCommentId())).isFalse();
    }

    @Test
    @DisplayName("실 DB 기준: 유저별 댓글 조회 성공")
    void get_comments_by_user_success() {

        // given
        CommentRequest req = new CommentRequest();
        req.setMovieId(EXIST_MOVIE_ID);
        req.setContent("유저별 조회");

        commentService.addComment(EXIST_EMAIL, req);

        // when
        List<CommentResponse> comments =
                commentService.getCommentsByUser(EXIST_EMAIL);

        // then
        assertThat(comments).isNotEmpty();
        assertThat(comments)
                .anyMatch(c -> c.getContent().equals("유저별 조회"));
    }
}
