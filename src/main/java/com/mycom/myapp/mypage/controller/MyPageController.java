package com.mycom.myapp.mypage.controller;

import com.mycom.myapp.favorite.service.FavoriteService;
import com.mycom.myapp.comment.service.CommentService;
import com.mycom.myapp.global.jwt.JwtTokenProvider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {

    private final FavoriteService favoriteService;
    private final CommentService commentService;
    private final JwtTokenProvider jwtTokenProvider;

    private String extractToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("accessToken".equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }

    @GetMapping("/favorites")
    public ResponseEntity<?> getFavorites(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token))
            return ResponseEntity.status(401).body("Unauthorized");

        String email = jwtTokenProvider.getAuthentication(token).getName();
        return ResponseEntity.ok(favoriteService.getFavorites(email));
    }

    @GetMapping("/comments")
    public ResponseEntity<?> getComments(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token))
            return ResponseEntity.status(401).body("Unauthorized");

        String email = jwtTokenProvider.getAuthentication(token).getName();
        return ResponseEntity.ok(commentService.getCommentsByUser(email));
    }
}
