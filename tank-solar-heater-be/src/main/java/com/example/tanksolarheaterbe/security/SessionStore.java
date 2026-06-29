package com.example.tanksolarheaterbe.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class SessionStore {

    private static final String KEY_PREFIX = "auth:session:";

    private final SecureRandom random = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    private final StringRedisTemplate redis;

    /** Stores {@code jwt} under a fresh session id with the given TTL and returns that id. */
    public String create(String jwt, Duration ttl) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String sessionId = encoder.encodeToString(bytes);
        redis.opsForValue().set(key(sessionId), jwt, ttl);
        return sessionId;
    }

    /** Returns the JWT for a session id, or empty if it is unknown or expired. */
    public Optional<String> resolve(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(redis.opsForValue().get(key(sessionId)));
    }

    /** Removes the session, immediately invalidating its cookie. */
    public void delete(String sessionId) {
        if (sessionId != null && !sessionId.isBlank()) {
            redis.delete(key(sessionId));
        }
    }

    private String key(String sessionId) {
        return KEY_PREFIX + sessionId;
    }
}
