package com.mycom.myapp.comment.repository;

import com.mycom.myapp.comment.entity.Comment;
import com.mycom.myapp.movie.entity.Movie;
import com.mycom.myapp.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByMovieOrderByCreatedAtDesc(Movie movie);

    List<Comment> findByUserOrderByCreatedAtDesc(User user);
}
