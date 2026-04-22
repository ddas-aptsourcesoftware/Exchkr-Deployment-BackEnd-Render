package com.exchkr.club.management.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.exchkr.club.management.dao.TokenBlacklistRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtil jwtUtil;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    public JwtAuthFilter(JwtUtil jwtUtil, TokenBlacklistRepository tokenBlacklistRepository) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/webhook/stripe/");
    }

    // NOW USING Authorization HEADER
    private String extractJwt(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = extractJwt(request);

        if (jwt != null) {
            try {
                if (jwtUtil.isTokenValid(jwt)) {

                    String jti = jwtUtil.extractJti(jwt);

                    if (jti != null && tokenBlacklistRepository.existsById(jti)) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }

                    String userEmail = jwtUtil.extractUsername(jwt);

                    if (userEmail != null &&
                            SecurityContextHolder.getContext().getAuthentication() == null) {

                        List<String> roles = jwtUtil.extractRoles(jwt);
                        Long clubId = jwtUtil.extractClubId(jwt);
                        Long userId = jwtUtil.extractUserId(jwt);

                        List<SimpleGrantedAuthority> authorities = roles.stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .collect(Collectors.toList());

                        CustomUserDetails userDetails =
                                new CustomUserDetails(userId, userEmail, clubId, authorities);

                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        authorities
                                );

                        // IMPORTANT FIX
                        authToken.setDetails(userDetails);

                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (JWTVerificationException e) {
                logger.error("Token validation failed: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}