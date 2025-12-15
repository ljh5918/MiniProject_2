package com.mycom.myapp.mypage.controller;

import com.mycom.myapp.favorite.service.FavoriteService;
import com.mycom.myapp.comment.service.CommentService;
import com.mycom.myapp.global.jwt.JwtTokenProvider;
import com.mycom.myapp.user.dto.NicknameUpdateDto;
import com.mycom.myapp.user.dto.PasswordUpdateDto;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.repository.UserRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {

    private final FavoriteService favoriteService;
    private final CommentService commentService;
    private final JwtTokenProvider jwtTokenProvider;
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


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
    
    // 닉네임 변경 API
    @PutMapping("/nickname")
    @Transactional
    public ResponseEntity<?> updateNickname(
            @RequestBody NicknameUpdateDto request,
            HttpServletRequest httpRequest) {

        String token = extractToken(httpRequest);
        if (token == null || !jwtTokenProvider.validateToken(token))
            return ResponseEntity.status(401).body("로그인이 필요합니다.");

        String email = jwtTokenProvider.getAuthentication(token).getName();

        if (userRepository.existsByNickname(request.getNickname())) {
            return ResponseEntity.badRequest().body("사용중인 nickname 입니다.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.updateNickname(request.getNickname());
        return ResponseEntity.ok("닉네임이 변경되었습니다.");
    }
    
    
    
    // 비밀번호 수정 API
    @PutMapping("/password")
    @Transactional
    public ResponseEntity<?> updatePassword(
            @RequestBody PasswordUpdateDto request,
            HttpServletRequest httpRequest) {

        String token = extractToken(httpRequest);
        if (token == null || !jwtTokenProvider.validateToken(token))
            return ResponseEntity.status(401).body("로그인이 필요합니다.");

        String email = jwtTokenProvider.getAuthentication(token).getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 🔐 현재 비밀번호 검증
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("현재 비밀번호가 일치하지 않습니다.");
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        return ResponseEntity.ok("비밀번호가 변경되었습니다.");
    }

    
    

}
