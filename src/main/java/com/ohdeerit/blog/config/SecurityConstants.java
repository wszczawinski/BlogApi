package com.ohdeerit.blog.config;

import java.time.Duration;

public final class SecurityConstants {
    private SecurityConstants() {
        throw new AssertionError("Constants class - cannot be instantiated");
    }

    public static final String JWT_COOKIE_NAME = "jwt";
}
