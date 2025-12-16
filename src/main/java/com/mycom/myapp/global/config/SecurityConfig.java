package com.mycom.myapp.global.config;

import com.mycom.myapp.global.jwt.JwtAuthenticationFilter;
import com.mycom.myapp.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (JWT 사용 시 필요)
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 적용
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 미사용
            
            .authorizeHttpRequests(auth -> auth
                // 1. 정적 자원 및 메인 페이지 (누구나 접근 가능)
                .requestMatchers(
                    "/", 
                    "/index.html",
                    "/register.html", 
                    "/login.html",
                    "/savedmovies.html", 
                    "/css/**", "/js/**", "/img/**",
                    "/test/**"
                ).permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // 2. 인증 관련 API (로그인/회원가입은 누구나 접근 가능)
                .requestMatchers(
                    "/user/login", 
                    "/user/register",
                    "/user/**" // 필요에 따라 user 하위 전체를 열거나 특정만 열 수 있음
                ).permitAll()

                // 3. 영화 정보 조회 (보통 목록/상세 조회는 비로그인도 가능)
                .requestMatchers("/movies/**").permitAll()

                // 4. 댓글 조회 (GET 방식은 누구나 가능)
                // 주의: 프론트엔드 API 경로가 /comment 인지 /comments 인지 확인하여 맞춤 설정
                .requestMatchers(HttpMethod.GET, "/comments/**", "/comment/**").permitAll()

                // 5. 인증이 필요한 페이지 및 기능
                // 댓글 작성/삭제, 찜하기, 마이페이지 등
                .requestMatchers(
                    "/comments/**", "/comment/**",
                    "/favorite/**", 
                    "/mypage/**", 
                    "/mypage.html"
                ).authenticated()

                // 6. 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )
            
            .formLogin(login -> login.disable())
            .httpBasic(basic -> basic.disable())

            // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS 설정
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        return request -> {
//            CorsConfiguration config = new CorsConfiguration();
//            // 프론트엔드 주소 (예: Live Server 사용 시 5500, 리액트 사용 시 3000 등)
//            config.setAllowedOrigins(List.of("http://localhost:8080", "http://127.0.0.1:5500")); 
//            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//            config.setAllowedHeaders(List.of("*"));
//            config.setAllowCredentials(true); // 쿠키 인증 허용
//            config.setMaxAge(3600L);
//            return config;
//        };
//    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*"
            ));
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(List.of("*"));
            config.setAllowCredentials(true);
            config.setMaxAge(3600L);
            return config;
        };
    }

    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}