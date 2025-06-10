package com.ohdeerit.blog.security;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import com.ohdeerit.blog.services.AuthenticationService;
import com.ohdeerit.blog.config.SecurityConstants;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.ServletException;
import lombok.RequiredArgsConstructor;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;


import java.io.IOException;
import java.util.Optional;
import java.util.Arrays;


@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final AuthenticationService authenticationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            authenticateRequest(request);
        } catch (ExpiredJwtException e) {
            log.debug("JWT token has expired");
            clearAuthenticationContext();
        } catch (JwtException e) {
            log.debug("Invalid JWT token");
            clearAuthenticationContext();
        } catch (Exception e) {
            log.error("Authentication error occurred", e);
            clearAuthenticationContext();
        }

        addSecurityHeaders(response);

        filterChain.doFilter(request, response);
    }

    private void authenticateRequest(HttpServletRequest request) {
        Optional<String> token = extractToken(request);

        token.ifPresent(jwt -> {
            UserDetails userDetails = authenticationService.validateToken(jwt);
            setAuthentication(userDetails, request);
        });
    }

    private void setAuthentication(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        if (userDetails instanceof BlogUserDetails blogUserDetails) {
            request.setAttribute("userId", blogUserDetails.getId());
        }
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> SecurityConstants.JWT_COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    private void clearAuthenticationContext() {
        SecurityContextHolder.clearContext();
    }

    private void addSecurityHeaders(HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
        response.setHeader("Pragma", "no-cache");
    }
}
