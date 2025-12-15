package com.mycom.myapp.test;

import com.mycom.myapp.user.dto.UserDto;
import com.mycom.myapp.user.dto.UserLoginDto;
import com.mycom.myapp.user.repository.UserRepository;
import com.mycom.myapp.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    
    
    @Test
    @DisplayName("실 DB 기준: 존재하지 않는 이메일이면 회원가입 성공")
    void register_success_when_email_not_exists_in_real_db() {

        // ⚠️ DB에 없는 이메일을 써야 함 (중요)
        String email = "test100@test.com";
        String password = "1234";
        String nickname = "test100";

        // 사전 확인: DB에 없어야 함
        assertThat(userRepository.findByEmail(email)).isEmpty();

        UserDto userDto = UserDto.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .build();

        // when
        userService.register(userDto);

        // then: DB에 실제로 저장되었는지 확인
        assertThat(userRepository.findByEmail(email)).isPresent();
    }

    
    
    
    @Test
    @DisplayName("실 DB 기준: 이미 존재하는 이메일이면 회원가입 실패")
    void register_fail_when_email_already_exists_in_real_db() {

        // ⚠️ 반드시 실 DB에 이미 존재하는 이메일
        String email = "asd@asd.com";

        assertThat(userRepository.findByEmail(email)).isPresent();

        UserDto userDto = UserDto.builder()
                .email(email)
                .password("anyPassword")
                .nickname("DupUser")
                .build();

        // when & then
        assertThatThrownBy(() -> userService.register(userDto))
                .isInstanceOf(Exception.class); 
                // 보통 DataIntegrityViolationException
    }

    
    
    @Test
    @DisplayName("실 DB에 존재하는 회원이면 로그인 성공")
    void login_success_when_user_exists_in_real_db() {

        // ⚠️ 반드시 실제 DB(users 테이블)에 존재하는 계정
        String email = "asd@asd.com";
        String password = "asd";

        // 안전 확인 (선택)
        assertThat(userRepository.findByEmail(email)).isPresent();

        UserLoginDto loginDto = new UserLoginDto(email, password);

        String token = userService.login(loginDto);

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("실 DB에 존재하지 않는 회원이면 로그인 실패")
    void login_fail_when_user_not_exists_in_real_db() {

        String email = "failemail@asd.com";
        String password = "1234";

        assertThat(userRepository.findByEmail(email)).isEmpty();

        UserLoginDto loginDto = new UserLoginDto(email, password);

        assertThatThrownBy(() -> userService.login(loginDto))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("실 DB에 회원은 있지만 비밀번호가 틀리면 로그인 실패")
    void login_fail_when_password_is_wrong() {

        // ⚠️ 실제 DB에 존재하는 이메일
        String email = "asd@asd.com";
        String wrongPassword = "failpw";

        assertThat(userRepository.findByEmail(email)).isPresent();

        UserLoginDto loginDto = new UserLoginDto(email, wrongPassword);

        assertThatThrownBy(() -> userService.login(loginDto))
                .isInstanceOf(BadCredentialsException.class);
    }
}
