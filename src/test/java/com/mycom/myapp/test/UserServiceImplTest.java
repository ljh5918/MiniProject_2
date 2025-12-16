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
    
        /* ---------------------------------
         * 이메일 중복 시 회원가입 실패
         --------------------------------- */
        @Test
        @DisplayName("실 DB 기준: 이미 존재하는 이메일이면 회원가입 실패")
        void register_fail_when_email_duplicate() {

            // ⚠️ 반드시 DB에 이미 존재하는 이메일
            String email = "asd@asd.com"; 
            String nickname = "uniqueNickname123"; // 닉네임은 중복되지 않게

            UserDto userDto = UserDto.builder()
                    .email(email)
                    .password("anyPassword")
                    .nickname(nickname)
                    .build();

            assertThatThrownBy(() -> userService.register(userDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("사용중인 email 입니다.");
        }

        /* ---------------------------------
         * 닉네임 중복 시 회원가입 실패
         --------------------------------- */
        @Test
        @DisplayName("실 DB 기준: 이미 존재하는 닉네임이면 회원가입 실패")
        void register_fail_when_nickname_duplicate() {

            // ⚠️ 반드시 DB에 이미 존재하는 닉네임
            String nickname = userRepository.findAll().get(0).getNickname(); 
            String email = "uniqueEmail123@test.com"; // 이메일은 중복되지 않게

            UserDto userDto = UserDto.builder()
                    .email(email)
                    .password("anyPassword")
                    .nickname(nickname)
                    .build();

            assertThatThrownBy(() -> userService.register(userDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("사용중인 nickname 입니다.");
        }

        /* ---------------------------------
         * 이메일 & 닉네임 모두 중복 시 회원가입 실패
         --------------------------------- */
        @Test
        @DisplayName("실 DB 기준: 이메일과 닉네임 모두 이미 존재하면 회원가입 실패")
        void register_fail_when_email_and_nickname_duplicate() {

            String email = "asd@asd.com"; // DB에 존재
            String nickname = userRepository.findAll().get(0).getNickname(); // DB에 존재

            UserDto userDto = UserDto.builder()
                    .email(email)
                    .password("anyPassword")
                    .nickname(nickname)
                    .build();

            assertThatThrownBy(() -> userService.register(userDto))
                    .isInstanceOf(IllegalArgumentException.class);
            // 메시지는 이메일 중복이 먼저 체크되므로 "사용중인 email 입니다." 나올 것
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
