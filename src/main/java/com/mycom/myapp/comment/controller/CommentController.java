package com.mycom.myapp.comment.controller;

import com.mycom.myapp.comment.dto.CommentRequest;
import com.mycom.myapp.comment.service.CommentService;
import com.mycom.myapp.global.jwt.JwtTokenProvider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;
    private final JwtTokenProvider jwtTokenProvider;

    private String extractToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("accessToken".equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }

    // 댓글 작성
    @PostMapping
    public ResponseEntity<?> addComment(@RequestBody CommentRequest req, HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token))
            return ResponseEntity.status(401).body("Unauthorized");

        String email = jwtTokenProvider.getAuthentication(token).getName();
        return ResponseEntity.ok(commentService.addComment(email, req));
    }

 // 댓글 조회
    @GetMapping("/{movieId}")
    public ResponseEntity<?> getComments(@PathVariable("movieId") Long movieId) {
        return ResponseEntity.ok(commentService.getComments(movieId));
    }

    // 댓글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable("commentId") Long commentId,
            @RequestBody String newContent,
            HttpServletRequest request
    ) {
        String token = extractToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token))
            return ResponseEntity.status(401).body("Unauthorized");

        String email = jwtTokenProvider.getAuthentication(token).getName();
        return ResponseEntity.ok(commentService.updateComment(email, commentId, newContent));
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable("commentId") Long commentId,
            HttpServletRequest request
    ) {
        String token = extractToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token))
            return ResponseEntity.status(401).body("Unauthorized");

        String email = jwtTokenProvider.getAuthentication(token).getName();
        commentService.deleteComment(email, commentId);
        return ResponseEntity.ok().build();
    }

}
