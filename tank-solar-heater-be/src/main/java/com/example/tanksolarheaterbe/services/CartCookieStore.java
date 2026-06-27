package com.example.tanksolarheaterbe.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CartCookieStore {

    public static final String COOKIE_NAME = "cart";
    private static final Duration MAX_AGE = Duration.ofDays(7);

    private final ObjectMapper mapper = new ObjectMapper();

    public Map<Integer, Integer> read(String cookieValue) {
        if (cookieValue == null || cookieValue.isEmpty()) {
            return new LinkedHashMap<>();
        }
        try {
            String json = URLDecoder.decode(cookieValue, StandardCharsets.UTF_8);
            return mapper.readValue(json, new TypeReference<LinkedHashMap<Integer, Integer>>() {});
        } catch (Exception ignored) {
            return new LinkedHashMap<>();
        }
    }

    public ResponseCookie buildCookie(Map<Integer, Integer> items) {
        String value;
        try {
            value = URLEncoder.encode(mapper.writeValueAsString(items), StandardCharsets.UTF_8);
        } catch (Exception e) {
            value = "";
        }
        return baseCookie(value, MAX_AGE);
    }

    public ResponseCookie clearCookie() {
        return baseCookie("", Duration.ZERO);
    }

    private ResponseCookie baseCookie(String value, Duration maxAge) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .path("/")
                .httpOnly(true)
                .sameSite("Lax")
                .maxAge(maxAge)
                .build();
    }
}
