package com.example.tanksolarheaterbe.security;

import com.example.tanksolarheaterbe.entities.Account;
import com.example.tanksolarheaterbe.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

/**
 * Manages opaque refresh tokens persisted on the {@link Account} row. Each account
 * holds at most one active token; issuing a new one (login or rotation) replaces it.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    /** Refresh-token lifetime; the RID cookie is scoped to the same window. */
    public static final Duration TTL = Duration.ofDays(7);

    private final SecureRandom random = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    private final AccountRepository accountRepository;

    /** Generates a fresh refresh token for the account, persists it, and returns the raw value. */
    @Transactional
    public String issue(Account account) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = encoder.encodeToString(bytes);

        account.setRefreshToken(token);
        account.setRefreshTokenExpiry(Instant.now().plus(TTL));
        accountRepository.save(account);

        return token;
    }

    @Transactional(readOnly = true)
    public Account resolveValid(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Missing refresh token");
        }

        Account account = accountRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        Instant expiry = account.getRefreshTokenExpiry();
        if (expiry == null || expiry.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token has expired");
        }

        return account;
    }

    @Transactional
    public void revoke(Account account) {
        account.setRefreshToken(null);
        account.setRefreshTokenExpiry(null);
        accountRepository.save(account);
    }
}
