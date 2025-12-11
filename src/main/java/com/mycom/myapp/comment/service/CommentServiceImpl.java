package com.mycom.myapp.comment.service;

import com.mycom.myapp.comment.dto.CommentRequest;
import com.mycom.myapp.comment.dto.CommentResponse;
import com.mycom.myapp.comment.entity.Comment;
import com.mycom.myapp.comment.repository.CommentRepository;
import com.mycom.myapp.movie.entity.Movie;
import com.mycom.myapp.movie.repository.MovieRepository;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    @Override
    @Transactional
    public CommentResponse addComment(String userEmail, CommentRequest req) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Movie movie = movieRepository.findById(req.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        Comment c = Comment.builder()
                .user(user)
                .movie(movie)
                .content(req.getContent())
                .build();

        return CommentResponse.fromEntity(commentRepository.save(c));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        return commentRepository.findByMovieOrderByCreatedAtDesc(movie)
                .stream()
                .map(CommentResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public CommentResponse updateComment(String userEmail, Long commentId, String newContent) {
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!c.getUser().getEmail().equals(userEmail))
            throw new SecurityException("Forbidden");

        c.setContent(newContent);
        return CommentResponse.fromEntity(commentRepository.save(c));
    }

    @Override
    @Transactional
    public void deleteComment(String userEmail, Long commentId) {
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!c.getUser().getEmail().equals(userEmail))
            throw new SecurityException("Forbidden");

        commentRepository.delete(c);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return commentRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(CommentResponse::fromEntity)
                .toList();
    }
}
