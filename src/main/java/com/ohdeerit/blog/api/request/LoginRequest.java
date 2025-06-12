package com.ohdeerit.blog.api.request;

public record LoginRequest(
        String email,
        String password
) {
}
