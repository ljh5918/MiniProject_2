package com.mycom.myapp.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long ACCESS_TOKEN_EXPIRE_TIME;
    private final String AUTHORITIES_KEY = "auth";

    public JwtTokenProvider(
        @Value("${jwt.secret}") String secretKey,
        @Value("${jwt.access-token-expire-time}") long accessTokenExpireTime
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.ACCESS_TOKEN_EXPIRE_TIME = accessTokenExpireTime;
    }

    /** Access Token 생성 */
    public String createAccessToken(Authentication authentication) {

        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = System.currentTimeMillis();
        Date expires = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .subject(authentication.getName()) // .setSubject() → .subject()로 변경됨
                .claim(AUTHORITIES_KEY, authorities)
                .expiration(expires) // .setExpiration() → .expiration() 로 변경됨
                .signWith(key) // 0.12.x에서는 알고리즘 자동 결정됨(HMAC)
                .compact();
    }

    /** 토큰에서 인증 정보 생성 */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        UserDetails principal = new User(
                claims.getSubject(),
                "",
                authorities
        );

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /** JWT 유효성 검증 */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(key) // setSigningKey → verifyWith 로 변경
                .build()
                .parseSignedClaims(token); // parseClaimsJws → parseSignedClaims
            return true;
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    /** Claims 파싱 */
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload(); // getBody → getPayload
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
