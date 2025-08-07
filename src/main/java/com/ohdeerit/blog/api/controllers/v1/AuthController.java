package com.ohdeerit.blog.api.controllers.v1;

import com.ohdeerit.blog.services.interfaces.AuthenticationService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import com.ohdeerit.blog.api.response.AuthResponse;
import com.ohdeerit.blog.api.request.LoginRequest;
import com.ohdeerit.blog.config.SecurityConstants;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping(path = "/login" )
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        final UserDetails userDetails = authenticationService.authenticate(
                loginRequest.email(),
                loginRequest.password());

        final String token = authenticationService.generateToken(userDetails);

        final ResponseCookie jwtCookie = ResponseCookie.from(SecurityConstants.JWT_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(SecurityConstants.SESSION_DURATION)
                .sameSite("Strict")
                .domain(null)
                .build();

        final ZonedDateTime expiresAt = ZonedDateTime.now().plusSeconds(SecurityConstants.SESSION_DURATION_SECONDS);
        final AuthResponse authResponse = new AuthResponse(expiresAt);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout() {
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
}