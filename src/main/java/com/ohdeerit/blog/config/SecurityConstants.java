package com.ohdeerit.blog.config;

import java.time.Duration;

public final class SecurityConstants {
    private SecurityConstants() {
        throw new AssertionError("Constants class - cannot be instantiated");
    }

    public static final String JWT_COOKIE_NAME = "jwt";
    public static final long SESSION_DURATION_SECONDS = 86400L; // 24 hours
    public static final Duration SESSION_DURATION = Duration.ofSeconds(SESSION_DURATION_SECONDS);
}