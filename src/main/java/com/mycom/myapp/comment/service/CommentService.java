package com.mycom.myapp.comment.service;

import java.util.List;
import com.mycom.myapp.comment.entity.Comment;

public interface CommentService {
    List<Comment> getCommentsByUser(String email);
}
