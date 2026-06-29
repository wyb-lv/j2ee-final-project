package com.example.tanksolarheaterbe.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

/**
 * Stores access JWTs in Redis keyed by an opaque session id. The client only ever holds that
 * session id (presented as a Bearer token); the token itself never leaves the server and the
 * entry expires automatically, so it behaves like a TTL cache.
 *
 * <p>Each session belongs to one account. A second {@link #create} for the same account revokes
 * that account's previous session (one active session per account), and deleting the key revokes
 * a session instantly (used on logout).
 */
@Service
@RequiredArgsConstructor
public class SessionStore {

    /** session id -> JWT */
    private static final String SESSION_PREFIX = "auth:session:";
    /** account -> its current session id (lets us revoke the previous session on re-login) */
    private static final String ACCOUNT_PREFIX = "auth:account:";

    private final SecureRandom random = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    private final StringRedisTemplate redis;

    /**
     * Stores {@code jwt} under a fresh session id owned by {@code accountKey} (e.g. the email),
     * replacing any session that account already had, and returns the new session id.
     */
    public String create(String accountKey, String jwt, Duration ttl) {
        revokeAccount(accountKey);

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String sessionId = encoder.encodeToString(bytes);

        redis.opsForValue().set(sessionKey(sessionId), jwt, ttl);
        redis.opsForValue().set(accountKey(accountKey), sessionId, ttl);
        return sessionId;
    }

    /** Returns the JWT for a session id, or empty if it is unknown or expired. */
    public Optional<String> resolve(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(redis.opsForValue().get(sessionKey(sessionId)));
    }

    /** Removes a single session by its id, immediately invalidating its token. */
    public void delete(String sessionId) {
        if (sessionId != null && !sessionId.isBlank()) {
            redis.delete(sessionKey(sessionId));
        }
    }

    /** Removes whichever session currently belongs to the account, if any. */
    public void revokeAccount(String accountKey) {
        if (accountKey == null || accountKey.isBlank()) {
            return;
        }
        String existing = redis.opsForValue().get(accountKey(accountKey));
        if (existing != null) {
            redis.delete(sessionKey(existing));
        }
        redis.delete(accountKey(accountKey));
    }

    private String sessionKey(String sessionId) {
        return SESSION_PREFIX + sessionId;
    }

    private String accountKey(String accountKey) {
        return ACCOUNT_PREFIX + accountKey;
    }
}
