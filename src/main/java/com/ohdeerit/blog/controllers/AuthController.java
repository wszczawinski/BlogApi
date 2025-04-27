package com.ohdeerit.blog.controllers;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import com.ohdeerit.blog.services.AuthenticationService;
import com.ohdeerit.blog.domain.dtos.LoginRequest;
import com.ohdeerit.blog.domain.dtos.AuthResponse;
import org.springframework.http.ResponseEntity;
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

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .expiresIn(86_400)
                .build();

        return ResponseEntity.ok(authResponse);
    }
}
