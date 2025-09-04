package com.medops.adapter.in.security;

import com.medops.domain.enums.TokenType;
import com.medops.application.port.out.LoadAdminPort;
import com.medops.application.port.out.LoadUserPort;
import com.medops.application.port.out.TokenPort;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final TokenPort tokenPort;
    private final LoadUserPort loadUserPort;
    private final LoadAdminPort loadAdminPort;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = null;
        
        // 1. Authorization 헤더에서 토큰 추출
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.isNotBlank(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            accessToken = authorizationHeader.substring(7);
        }
        
        // 2. 헤더에 토큰이 없으면 쿼리 파라미터에서 추출 (SSE용)
        if (StringUtils.isBlank(accessToken)) {
            accessToken = request.getParameter("token");
        }

        if (StringUtils.isNotBlank(accessToken)) {
            try {
                TokenType tokenType = tokenPort.parseTokenType(accessToken);
                String id = tokenPort.parseUserIdFromToken(accessToken);

                UserDetails userDetails = loadUserDetails(tokenType, id);
                setAuthentication(userDetails);

            } catch (JwtException e) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private void setAuthentication(UserDetails userDetails){
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
            )
        );
    }

    private UserDetails loadUserDetails(TokenType tokenType, String id) {
        if (tokenType == TokenType.USER) {
            return loadUserPort.loadUserById(id)
               .orElseThrow(() -> new UsernameNotFoundException("not found user"));
        } else {
            return loadAdminPort.loadAdminById(id)
                .orElseThrow(() -> new UsernameNotFoundException("not found admin"));
        }
    }
}
