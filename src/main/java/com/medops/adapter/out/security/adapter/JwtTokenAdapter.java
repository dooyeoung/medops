package com.medops.adapter.out.security.adapter;

import com.medops.application.port.out.TokenPort;
import com.medops.domain.enums.TokenType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@AllArgsConstructor
@RequiredArgsConstructor
public class JwtTokenAdapter implements TokenPort {

    @Value(value = "${secret.key}") String secretKey;
    @Value(value = "${secret.expiration_hours}") Long expirationHours;

    @Override
    public String generateToken(String id, TokenType tokenType) {
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiredAt = Date.from(now.plus(expirationHours, ChronoUnit.HOURS));

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("tokenType", tokenType);

        return Jwts.builder()
            .signWith(Keys.hmacShaKeyFor(secretKey.getBytes())).subject(id)
            .claims(claims)
            .issuedAt(issuedAt)
            .expiration(expiredAt)
            .compact();
    }

    @Override
    public String parseUserIdFromToken(String token) {
        try {
            // 어드민인지 사용자인지 확인 하기
            return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        } catch (ExpiredJwtException e) {
            throw new JwtException("토큰이 만료되었습니다.", e);
        } catch (UnsupportedJwtException e) {
            throw new JwtException("지원되지 않는 토큰 형식입니다.", e);
        } catch (MalformedJwtException e) {
            throw new JwtException("유효하지 않은 토큰입니다.", e);
        } catch (SignatureException e) {
            throw new JwtException("서명이 유효하지 않은 토큰입니다.", e);
        } catch (IllegalArgumentException e) {
            throw new JwtException("토큰이 비어있습니다.", e);
        }
    }

    @Override
    public TokenType parseTokenType(String token) {
        try {
            String tokenType = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("tokenType")
                .toString();
            return TokenType.valueOf(tokenType);
        } catch (ExpiredJwtException e) {
            throw new JwtException("토큰이 만료되었습니다.", e);
        } catch (UnsupportedJwtException e) {
            throw new JwtException("지원되지 않는 토큰 형식입니다.", e);
        } catch (MalformedJwtException e) {
            throw new JwtException("유효하지 않은 토큰입니다.", e);
        } catch (SignatureException e) {
            throw new JwtException("서명이 유효하지 않은 토큰입니다.", e);
        } catch (IllegalArgumentException e) {
            throw new JwtException("토큰이 비어있습니다.", e);
        }
    }
}
