package com.example.tanksolarheaterbe.security;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Issues access tokens (JWTs). Verification of incoming tokens is handled by Spring
 * Security's resource server (see {@link SecurityConfig}).
 */
@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;

    public JwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    /**
     * @param email subject of the token
     * @param role  account role (e.g. "admin"); stored as the {@code role} claim
     *              and later mapped to a {@code ROLE_*} authority.
     */
    public String generateToken(String email, String role) {

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(email)
                .issuedAt(now)
                // Short-lived: clients renew it via the refresh-token endpoint.
                .expiresAt(now.plus(15, ChronoUnit.MINUTES))
                .claim("role", role.toUpperCase())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }
}
