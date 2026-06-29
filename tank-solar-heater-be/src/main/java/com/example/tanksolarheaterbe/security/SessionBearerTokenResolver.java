package com.example.tanksolarheaterbe.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.stereotype.Component;

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
