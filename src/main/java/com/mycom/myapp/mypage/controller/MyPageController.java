package com.mycom.myapp.mypage.controller;

import com.mycom.myapp.favorite.service.FavoriteService;
import com.mycom.myapp.comment.service.CommentService;
import com.mycom.myapp.user.dto.NicknameUpdateDto;
import com.mycom.myapp.user.dto.PasswordUpdateDto;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {

    private final FavoriteService favoriteService;
    private final CommentService commentService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // --------------------------------------------------
    // 닉네임 변경
    // --------------------------------------------------
    @PutMapping("/nickname")
    @Transactional
    public ResponseEntity<?> updateNickname(
            @RequestBody NicknameUpdateDto request,
            Authentication authentication) {

        String email = authentication.getName();

        if (userRepository.existsByNickname(request.getNickname())) {
            return ResponseEntity.badRequest().body("사용중인 nickname 입니다.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.updateNickname(request.getNickname());
        return ResponseEntity.ok("닉네임이 변경되었습니다.");
    }

    // --------------------------------------------------
    // 비밀번호 변경
    // --------------------------------------------------
    @PutMapping("/password")
    @Transactional
    public ResponseEntity<?> updatePassword(
            @RequestBody PasswordUpdateDto request,
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("현재 비밀번호가 일치하지 않습니다.");
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        return ResponseEntity.ok("비밀번호가 변경되었습니다.");
    }

    // --------------------------------------------------
    // 즐겨찾기 / 댓글 조회 (optional)
    // --------------------------------------------------
    @GetMapping("/favorites")
    public ResponseEntity<?> getFavorites(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(favoriteService.getFavorites(email));
    }

    @GetMapping("/comments")
    public ResponseEntity<?> getComments(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(commentService.getCommentsByUser(email));
    }
}
