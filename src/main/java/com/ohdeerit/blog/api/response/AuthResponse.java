package com.ohdeerit.blog.api.response;

import java.time.LocalDateTime;

public record AuthResponse(
        LocalDateTime expiresAt
) {
}
