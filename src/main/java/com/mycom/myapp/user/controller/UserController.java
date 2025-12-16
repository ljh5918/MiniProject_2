package com.mycom.myapp.user.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.user.dto.UserDto;
import com.mycom.myapp.user.dto.UserLoginDto;
import com.mycom.myapp.user.dto.UserResultDto;
import com.mycom.myapp.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController	
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDto userDto) {
        try {
            UserResultDto result = userService.register(userDto);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()      // ⭐ 400
                    .body(e.getMessage());
        }
    }

	  @PostMapping("/login")
	  public ResponseEntity<Map<String, Object>> login(@RequestBody UserLoginDto userLoginDto) {
	      Map<String, Object> result = new HashMap<>();
	      try {
	          // 로그인 시도
	          String accessToken = userService.login(userLoginDto);

	          // Access Token을 HttpOnly Cookie에 담기
	          ResponseCookie cookie = ResponseCookie.from("accessToken", accessToken)
	                  .httpOnly(true)
	                  .secure(false)
	                  .sameSite("Lax")
	                  .maxAge(60 * 30)
	                  .path("/")
	                  .build();

	          result.put("status", "success");
	          result.put("message", "로그인 성공");
	          result.put("redirect", "/");  

	          return ResponseEntity.ok()
	                  .header(HttpHeaders.SET_COOKIE, cookie.toString())
	                  .body(result);

	      } catch (Exception e) {
	          result.put("status", "fail");
	          result.put("message", "로그인 실패.");
	          return ResponseEntity.status(401).body(result);
	      }
	  }

    
    
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        Map<String, Object> result = new HashMap<>();

        // 쿠키 만료 처리
        ResponseCookie cookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)      // 개발 환경: false / HTTPS 배포 시: true
                .sameSite("Lax")
                .maxAge(0)          // 즉시 만료
                .path("/")
                .build();

        result.put("status", "success");
        result.put("message", "로그아웃 성공");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(result);
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        // JwtTokenProvider.getAuthentication() 에서 subject = email 로 설정했음
        String email = authentication.getName();

        UserDto user = userService.getUserProfile(email);

        return ResponseEntity.ok(user);
    }


    

}
