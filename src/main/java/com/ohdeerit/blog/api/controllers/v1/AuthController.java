package com.ohdeerit.blog.api.controllers.v1;

import org.springframework.security.core.context.SecurityContextHolder;
import com.ohdeerit.blog.services.interfaces.AuthenticationService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import com.ohdeerit.blog.api.response.AuthResponse;
import com.ohdeerit.blog.api.request.LoginRequest;
import com.ohdeerit.blog.config.SecurityConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.ZonedDateTime;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    @Value("${jwt.session-duration-seconds}")
    private Long jwtSessionDurationSeconds;

    private final AuthenticationService authenticationService;

    @PostMapping(path = "/login" )
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            final UserDetails userDetails = authenticationService.authenticate(
                    loginRequest.email(),
                    loginRequest.password());

            final String token = authenticationService.generateToken(userDetails);

            // Log successful login
            log.info("AUTH_SUCCESS | user={} | ip={}", 
                    userDetails.getUsername(), getClientIp(request));

            final ResponseCookie jwtCookie = ResponseCookie.from(SecurityConstants.JWT_COOKIE_NAME, token)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(Duration.ofSeconds(jwtSessionDurationSeconds).toMillis())
                    .sameSite("Strict")
                    .domain(null)
                    .build();

            final ZonedDateTime expiresAt = ZonedDateTime.now().plusSeconds(jwtSessionDurationSeconds);
            final AuthResponse authResponse = new AuthResponse(expiresAt);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(authResponse);
        } catch (Exception e) {
            // Log failed login
            log.warn("AUTH_FAILURE | user={} | ip={} | reason={}", 
                    loginRequest.email(), getClientIp(request), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            log.info("AUTH_LOGOUT | user={} | ip={}", 
                    authentication.getName(), getClientIp(request));
        }

        ResponseCookie jwtCookie = ResponseCookie.from(SecurityConstants.JWT_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .domain(null)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new AuthResponse(null));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}