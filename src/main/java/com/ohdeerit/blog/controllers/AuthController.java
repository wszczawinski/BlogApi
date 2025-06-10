package com.ohdeerit.blog.controllers;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import com.ohdeerit.blog.services.AuthenticationService;
import com.ohdeerit.blog.config.SecurityConstants;
import com.ohdeerit.blog.domain.dtos.LoginRequest;
import com.ohdeerit.blog.domain.dtos.AuthResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        UserDetails userDetails = authenticationService.authenticate(
                loginRequest.getEmail(),
                loginRequest.getPassword());

        String token = authenticationService.generateToken(userDetails);

        ResponseCookie jwtCookie = ResponseCookie.from(SecurityConstants.JWT_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(SecurityConstants.SESSION_DURATION)
                .sameSite("Strict")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .expiresIn(SecurityConstants.SESSION_DURATION_SECONDS)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(authResponse);
    }
}