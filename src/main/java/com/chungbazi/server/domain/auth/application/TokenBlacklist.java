package com.chungbazi.server.domain.auth.application;

import java.time.Duration;

public interface TokenBlacklist {
    void add(String accessToken, Duration expiration);
    boolean contains(String accessToken);
}
