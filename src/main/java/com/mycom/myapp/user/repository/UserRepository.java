package com.mycom.myapp.user.repository;

import com.mycom.myapp.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional; // import 추가

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    
    // ⭐ 로그인 시 이메일로 사용자 전체 정보 조회
    Optional<User> findByEmail(String email); 
    
    // ⭐ 닉네임 중복 체크
    boolean existsByNickname(String nickname);
}