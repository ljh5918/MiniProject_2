package com.mycom.myapp.comment.dto;

import com.mycom.myapp.comment.entity.Comment;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentResponse {

    private Long commentId;
    private Long movieId;
    private String userEmail;
    private String userNickname;
    private String content;
    private String createdAt;

    public static CommentResponse fromEntity(Comment c) {
        return CommentResponse.builder()
                .commentId(c.getCommentId())
                .movieId(c.getMovie().getMovieId())
                .userEmail(c.getUser().getEmail())
                .userNickname(c.getUser().getNickname())
                .content(c.getContent())
                .createdAt(c.getCreatedAt().toString())
                .build();
    }
}
