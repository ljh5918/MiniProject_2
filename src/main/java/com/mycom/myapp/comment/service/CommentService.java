package com.mycom.myapp.comment.service;

import com.mycom.myapp.comment.dto.CommentRequest;
import com.mycom.myapp.comment.dto.CommentResponse;

import java.util.List;

public interface CommentService {

    CommentResponse addComment(String userEmail, CommentRequest req);

    List<CommentResponse> getComments(Long movieId);

    CommentResponse updateComment(String userEmail, Long commentId, String newContent);

    void deleteComment(String userEmail, Long commentId);

    List<CommentResponse> getCommentsByUser(String userEmail);
}
