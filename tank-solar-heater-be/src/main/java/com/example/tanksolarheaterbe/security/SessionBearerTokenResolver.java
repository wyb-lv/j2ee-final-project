package com.example.tanksolarheaterbe.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.stereotype.Component;

/**
 * Treats the {@code Authorization: Bearer} value as an opaque session id and swaps it for the
 * access JWT held in Redis, so the existing signature/expiry validation runs unchanged. If the
 * presented value is not a known session (e.g. a raw JWT pasted into Swagger), it is passed
 * through as-is.
 */
@Component
@RequiredArgsConstructor
public class SessionBearerTokenResolver implements BearerTokenResolver {

    private final SessionStore sessionStore;
    private final DefaultBearerTokenResolver delegate = new DefaultBearerTokenResolver();

    @Override
    public String resolve(HttpServletRequest request) {
        String presented = delegate.resolve(request);
        if (presented == null) {
            return null;
        }
        return sessionStore.resolve(presented).orElse(presented);
    }
}
