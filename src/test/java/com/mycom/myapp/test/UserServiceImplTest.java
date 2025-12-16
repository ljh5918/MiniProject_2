package com.mycom.myapp.test;

import com.mycom.myapp.global.jwt.JwtTokenProvider;
import com.mycom.myapp.user.dto.UserDto;
import com.mycom.myapp.user.dto.UserLoginDto;
import com.mycom.myapp.user.dto.UserResultDto;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.repository.UserRepository;
import com.mycom.myapp.user.service.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
<<<<<<< Updated upstream
    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManagerBuilder authenticationManagerBuilder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private Authentication authentication;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
    }

    @Test
    void registerTest() {
        UserDto input = UserDto.builder()
                .email("test@test.com")
                .password("1234")
                .nickname("tester")
                .build();

        User saved = User.builder()
                .userId(1L)
                .email("test@test.com")
                .nickname("tester")
                .password("encoded")
                .role("ROLE_USER")
                .build();

        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(saved);

        UserResultDto result = userService.register(input);

        assertEquals("test@test.com", result.getEmail());
        assertEquals("tester", result.getNickname());
    }
=======
    
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
    

>>>>>>> Stashed changes

//    @Test
//    void loginTest() {
//        UserLoginDto loginDto = new UserLoginDto("test@test.com", "1234");
//
//        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
//                .thenReturn(authentication);
//
//        when(jwtTokenProvider.createAccessToken(any())).thenReturn("dummyToken");
//
//        String token = userService.login(loginDto);
//
//        assertEquals("dummyToken", token);
//    }
    

    
    
    @Test
    @DisplayName("로그인 성공 테스트")
    void loginTest() {
        UserLoginDto loginDto = new UserLoginDto("test@test.com", "1234");

        // ⭐ DB에 해당 유저가 존재하는 것처럼 설정
        User user = User.builder()
                .email("test@test.com")
                .password("encodedPw") // 실제 검증은 AuthenticationManager가 수행
                .nickname("tester")
                .build();

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        // authenticationManager 동작 mock
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtTokenProvider.createAccessToken(any(Authentication.class)))
                .thenReturn("dummyToken");

        String token = userService.login(loginDto);

        assertEquals("dummyToken", token);
    }
    

    @Test
    void getUserProfileTest() {
        User user = User.builder()
                .email("test@test.com")
                .nickname("tester")
                .password("1234")
                .build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        UserDto dto = userService.getUserProfile("test@test.com");

        assertEquals("test@test.com", dto.getEmail());
        assertEquals("tester", dto.getNickname());
    }
}





















//
//package com.mycom.myapp.test;
//
//
//import com.mycom.myapp.user.dto.UserDto;
//import com.mycom.myapp.user.dto.UserLoginDto;
//import com.mycom.myapp.user.repository.UserRepository;
//import com.mycom.myapp.user.service.UserService;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.transaction.annotation.Transactional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
//@SpringBootTest // 1. 실제 스프링 컨테이너를 로드합니다 (DB 연결 포함)
//@Transactional  // 2. 테스트가 끝나면 데이터를 롤백하여 DB를 깨끗하게 유지합니다
//class UserServiceImplTest {
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @BeforeEach
//    void setup() {
//        // 테스트 시작 전 DB 초기화가 필요하다면 여기서 수행
//        // userRepository.deleteAll(); 
//    }
//
//    @Test
//    @DisplayName("실제 DB 연동: 회원가입 후 로그인 성공 테스트")
//    void registerAndLogin_success() {
//        // 1. 회원가입 (실제 DB에 저장됨)
//        UserDto joinDto = UserDto.builder()
//                .email("real@test.com")
//                .password("realPassword123")
//                .nickname("RealUser")
//                .build();
//        
//        userService.register(joinDto);
//
//        // 2. 로그인 시도
//        UserLoginDto loginDto = new UserLoginDto("realnn@test.com", "realPassword123");
//        String accessToken = userService.login(loginDto);
//
//        // 3. 검증
//        assertThat(accessToken).isNotNull();
//        System.out.println("발급된 토큰: " + accessToken);
//    }
//
//    @Test
//    @DisplayName("실제 DB 연동: 비밀번호 불일치 시 로그인 실패")
//    void login_fail_wrong_password() {
//        // 1. 회원가입
//        UserDto joinDto = UserDto.builder()
//                .email("fail@test.com")
//                .password("correctPassword")
//                .nickname("FailUser")
//                .build();
//        userService.register(joinDto);
//
//        // 2. 틀린 비밀번호로 로그인 시도
//        UserLoginDto loginDto = new UserLoginDto("fail@test.com", "WRONG_PASSWORD");
//
//        // 3. 예외 발생 검증 (Spring Security 설정에 따라 BadCredentialsException 발생)
//        assertThatThrownBy(() -> userService.login(loginDto))
//                .isInstanceOf(BadCredentialsException.class);
//    }
//    
//    @Test
//    @DisplayName("실제 DB 연동: 존재하지 않는 이메일 로그인 실패")
//    void login_fail_no_user() {
//        // 가입하지 않은 데이터
//        UserLoginDto loginDto = new UserLoginDto("ghost@test.com", "anyPassword");
//
//        assertThatThrownBy(() -> userService.login(loginDto))
//                .isInstanceOf(BadCredentialsException.class); 
//                // 혹은 InternalAuthenticationServiceException 등 설정에 따라 다를 수 있음
//    }
//}
//
//
