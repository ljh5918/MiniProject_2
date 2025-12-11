////package com.mycom.myapp.global.config;
////
////import com.mycom.myapp.global.jwt.JwtAuthenticationFilter; 
////import com.mycom.myapp.global.jwt.JwtTokenProvider; 
////import lombok.RequiredArgsConstructor; 
////import org.springframework.context.annotation.Bean;
////import org.springframework.context.annotation.Configuration;
////import org.springframework.security.authentication.AuthenticationManager; 
////import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; 
////import org.springframework.security.config.annotation.web.builders.HttpSecurity;
////import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
////import org.springframework.security.config.http.SessionCreationPolicy; 
////import org.springframework.security.web.SecurityFilterChain;
////import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; 
////import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
////import org.springframework.security.crypto.password.PasswordEncoder;
////
////@Configuration
////@EnableWebSecurity
////@RequiredArgsConstructor
////public class SecurityConfig {
////
////    private final JwtTokenProvider jwtTokenProvider; 
////
////    @Bean
////    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
////        http
////                .csrf(csrf -> csrf.disable()) 
////                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 미사용(JWT)
////
////                .authorizeHttpRequests(auth -> auth
////                        .requestMatchers(
////                        		"/",
////                        		"/index.html", 
////                        		"/register.html",
////                        		"/login.html",
////                        		"/test/**",
////                        		"/css/**", "/js/**", "/img/**",
////                        		"/favorite/**",
////                        		"/mypage/**",
////                        		"/mypage.html",
////                        		"/comment/**",
////                        		"/user/**"
////                        		).permitAll()  // 회원가입/로그인 모두 허용
////                        .anyRequest().authenticated()             
////                )
////                .formLogin(login -> login.disable()) 
////                .httpBasic(basic -> basic.disable())
////                
////                // JWT 필터를 인증 필터 이전에 등록
////                .addFilterBefore(
////                    new JwtAuthenticationFilter(jwtTokenProvider), 
////                    UsernamePasswordAuthenticationFilter.class
////                ); 
////
////        return http.build();
////    }
////    
////    // AuthenticationManager Bean 등록
////    @Bean
////    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
////        return authenticationConfiguration.getAuthenticationManager();
////    }
////
////    @Bean
////    public PasswordEncoder passwordEncoder() {
////        return new BCryptPasswordEncoder();
////    }
////}
//
//
//package com.mycom.myapp.global.config;
//
//import com.mycom.myapp.global.jwt.JwtAuthenticationFilter;
//import com.mycom.myapp.global.jwt.JwtTokenProvider;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//@Configuration
//@EnableWebSecurity
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    private final JwtTokenProvider jwtTokenProvider;
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//            .csrf(csrf -> csrf.disable())
//            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//            .authorizeHttpRequests(auth -> auth
//                // 공개 허용: 메인/정적/로그인/회원가입/테스트
//                .requestMatchers(
//                    "/", "/index.html",
//                    "/register.html", "/login.html",
//                    "/test/**",
//                    "/css/**", "/js/**", "/img/**",
//                    "/user/login", "/user/register",
//                    "/comments/**"
//                ).permitAll()
//
//                // 인증 필요: 댓글, 찜, 마이페이지 등은 로그인 필요
//                .requestMatchers("/comment/**", "/favorite/**", "/mypage/**").authenticated()
//
//                // 그 외는 인증 필요
//                .anyRequest().authenticated()
//            )
//            .formLogin(login -> login.disable())
//            .httpBasic(basic -> basic.disable())
//
//            // JWT 필터 등록
//            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
//        return authenticationConfiguration.getAuthenticationManager();
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//}



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
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 공개 허용: 메인/정적/로그인/회원가입/테스트
                .requestMatchers(
                    "/", "/index.html",
                    "/register.html", "/login.html",
                    "/test/**",
                    "/css/**", "/js/**", "/img/**",
                    "/user/login", "/user/register"
                ).permitAll()

                // 댓글 조회(GET)은 공개 허용 (누구나 볼 수 있게)
                .requestMatchers(HttpMethod.GET, "/comments/**").permitAll()

                // 댓글 쓰기/수정/삭제는 인증 필요
                .requestMatchers("/comments/**").authenticated()

                // 찜, 마이페이지 등은 인증 필요
                .requestMatchers("/favorite/**", "/mypage/**").authenticated()

                // 그 외는 인증 필요
                .anyRequest().authenticated()
            )
            .formLogin(login -> login.disable())
            .httpBasic(basic -> basic.disable())

            // JWT 필터 등록
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS 설정: 개발 중에는 localhost 포트를 적절히 추가하세요.
    private CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();
            // 허용 origin은 실제 개발 환경에 맞게 바꾸세요.
            config.setAllowedOrigins(List.of("http://localhost:8080")); 
            config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
            config.setAllowedHeaders(List.of("*"));
            config.setAllowCredentials(true); // 중요: 쿠키 인증(credential) 허용
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
