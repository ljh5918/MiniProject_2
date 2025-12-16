package com.mycom.myapp.user.repository;

import com.mycom.myapp.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional; // import 추가

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    
    Optional<User> findByEmail(String email);

	boolean existsByNickname(String nickname); 

}