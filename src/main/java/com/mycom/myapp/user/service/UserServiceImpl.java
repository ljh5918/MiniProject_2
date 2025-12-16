package com.mycom.myapp.user.service;

import com.mycom.myapp.user.dto.UserDto;
import com.mycom.myapp.user.dto.UserResultDto;
import com.mycom.myapp.user.dto.UserLoginDto; // import 추가
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.repository.UserRepository;
import com.mycom.myapp.global.jwt.JwtTokenProvider; // import 추가
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder; // import 추가
import org.springframework.security.core.Authentication; // import 추가
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder; 
    private final JwtTokenProvider jwtTokenProvider; 

    
    @Override
    @Transactional
    public UserResultDto register(UserDto userDto) {

        // 1️⃣ 이메일 중복 체크
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("사용중인 email 입니다.");
        }

        // 2️⃣ 닉네임 중복 체크
        if (userRepository.existsByNickname(userDto.getNickname())) {
            throw new IllegalArgumentException("사용중인 nickname 입니다.");
        }

        String encodedPw = passwordEncoder.encode(userDto.getPassword());

        User user = User.builder()
                .email(userDto.getEmail())
                .password(encodedPw)
                .nickname(userDto.getNickname())
                .role("ROLE_USER")
                .build();

        User saved = userRepository.save(user);

        return new UserResultDto(
                saved.getUserId(),
                saved.getEmail(),
                saved.getNickname()
        );
    }

    
    
    // ⭐ [추가] 로그인 로직 구현
    @Override
    @Transactional
    public String login(UserLoginDto userLoginDto) {
        // 1. Username/Password 기반 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = 
            new UsernamePasswordAuthenticationToken(userLoginDto.getEmail(), userLoginDto.getPassword());

        // 2. 실제 인증 (비밀번호 검증)
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT Access Token 생성
        String accessToken = jwtTokenProvider.createAccessToken(authentication);

        return accessToken;
    }
    
    @Override
    public UserDto getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return UserDto.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }

}



