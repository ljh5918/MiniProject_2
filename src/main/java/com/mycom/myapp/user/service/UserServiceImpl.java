
package com.mycom.myapp.user.service;

import com.mycom.myapp.user.dto.UserDto;

import com.mycom.myapp.user.dto.UserResultDto;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResultDto register(UserDto userDto) {

        // 비밀번호 암호화
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

}




