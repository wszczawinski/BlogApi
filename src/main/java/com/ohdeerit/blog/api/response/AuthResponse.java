package com.ohdeerit.blog.api.response;

import java.time.ZonedDateTime;

public record AuthResponse(
        ZonedDateTime expiresAt
) {
}
