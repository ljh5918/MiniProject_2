package com.mycom.myapp.comment.service;

import java.util.List;

import org.springframework.stereotype.Service;
import com.mycom.myapp.comment.entity.Comment;
import com.mycom.myapp.comment.repository.CommentRepository;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Override
    public List<Comment> getCommentsByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return commentRepository.findByUser(user);
    }
}
