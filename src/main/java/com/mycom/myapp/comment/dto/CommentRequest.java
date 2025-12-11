package com.mycom.myapp.comment.dto;

import lombok.Data;

@Data
public class CommentRequest {
    private Long movieId;
    private String content;
}
