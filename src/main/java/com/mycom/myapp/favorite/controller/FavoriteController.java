package com.mycom.myapp.favorite.controller;

import com.mycom.myapp.favorite.service.FavoriteService;
import com.mycom.myapp.global.jwt.JwtTokenProvider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final JwtTokenProvider jwtTokenProvider;

    private String extractToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addFavorite(@RequestBody Map<String, Object> body,
                                         HttpServletRequest request) {

        String token = extractToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String email = jwtTokenProvider.getAuthentication(token).getName();

        Long movieId = ((Number) body.get("movieId")).longValue();
        String title = (String) body.get("title");
        String posterPath = (String) body.get("posterPath");

        favoriteService.addFavorite(email, movieId, title, posterPath);

        return ResponseEntity.ok("success");
    }
    
    
    @GetMapping("/list")
    public ResponseEntity<?> getFavorites(HttpServletRequest request) {

        String token = extractToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String email = jwtTokenProvider.getAuthentication(token).getName();

        return ResponseEntity.ok(favoriteService.getFavorites(email));
    }

    
    // ✅ 찜 삭제 API
    @PostMapping("/delete")
    public ResponseEntity<?> deleteFavorite(@RequestBody Map<String, Object> body,
                                            HttpServletRequest request) {

        String token = extractToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String email = jwtTokenProvider.getAuthentication(token).getName();
        Long movieId = ((Number) body.get("movieId")).longValue();

        favoriteService.deleteFavorite(email, movieId);
        return ResponseEntity.ok("삭제 완료");
    }


}
