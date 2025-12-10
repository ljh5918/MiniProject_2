package com.mycom.myapp.global.config;

import com.mycom.myapp.global.jwt.JwtAuthenticationFilter; 
import com.mycom.myapp.global.jwt.JwtTokenProvider; 
import lombok.RequiredArgsConstructor; 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager; 
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; 
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy; 
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; 
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider; 

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) 
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 미사용(JWT)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                        		"/",
                        		"/index.html", 
                        		"/register.html",
                        		"/login.html",
                        		"/user/**").permitAll()  // 회원가입/로그인 모두 허용
                        .anyRequest().authenticated()             
                )
                .formLogin(login -> login.disable()) 
                .httpBasic(basic -> basic.disable())
                
                // JWT 필터를 인증 필터 이전에 등록
                .addFilterBefore(
                    new JwtAuthenticationFilter(jwtTokenProvider), 
                    UsernamePasswordAuthenticationFilter.class
                ); 

        return http.build();
    }
    
    // AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
