package com.mycom.myapp.comment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.mycom.myapp.comment.entity.Comment;
import com.mycom.myapp.user.entity.User;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByUser(User user);
}
