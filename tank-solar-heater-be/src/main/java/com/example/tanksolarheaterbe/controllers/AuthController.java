package com.example.tanksolarheaterbe.controllers;

import com.example.tanksolarheaterbe.dto.LoginRequest;
import com.example.tanksolarheaterbe.dto.LoginResponse;
import com.example.tanksolarheaterbe.dto.RefreshTokenRequest;
import com.example.tanksolarheaterbe.dto.TokenResponse;
import com.example.tanksolarheaterbe.dto.UserRequest;
import com.example.tanksolarheaterbe.dto.UserResponse;
import com.example.tanksolarheaterbe.entities.Account;
import com.example.tanksolarheaterbe.repositories.AccountRepository;
import com.example.tanksolarheaterbe.security.JwtService;
import com.example.tanksolarheaterbe.security.RefreshTokenService;
import com.example.tanksolarheaterbe.security.SessionStore;
import com.example.tanksolarheaterbe.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AccountRepository accountRepository;
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final SessionStore sessionStore;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow();

        String sessionId = openSession(account);
        String refreshToken = refreshTokenService.issue(account);

        return new LoginResponse(
                sessionId, refreshToken, account.getId(), account.getName(), account.getRole());
    }

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody UserRequest request) {
        return authService.register(request);
    }

    /** Exchanges a valid refresh token for a new session id and a rotated refresh token. */
    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {

        Account account;
        try {
            account = refreshTokenService.resolveValid(request.refreshToken());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }

        String sessionId = openSession(account);
        String rotated = refreshTokenService.issue(account);

        return new TokenResponse(sessionId, rotated);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {

        // Revoke the account's session (and refresh token) so nothing lingers in the cache.
        if (authentication != null && authentication.isAuthenticated()) {
            sessionStore.revokeAccount(authentication.getName());
            accountRepository.findByEmail(authentication.getName())
                    .ifPresent(refreshTokenService::revoke);
        }

        return ResponseEntity.noContent().build();
    }

    /** Mints a JWT for the account and stores it in Redis under that account, returning the session id. */
    private String openSession(Account account) {
        String jwt = jwtService.generateToken(account.getEmail(), account.getRole());
        return sessionStore.create(account.getEmail(), jwt, JwtService.ACCESS_TTL);
    }
}
