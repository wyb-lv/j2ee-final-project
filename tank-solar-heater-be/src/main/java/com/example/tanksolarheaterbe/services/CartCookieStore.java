package com.example.tanksolarheaterbe.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stores the cart in an HTTP cookie ("cart") as a URL-encoded JSON map of
 * productId -> quantity. The server is stateless: the cart travels with the
 * client on every request and is written back on every mutation.
 */
@Component
public class CartCookieStore {

    private static final String COOKIE_NAME = "cart";
    private static final Duration MAX_AGE = Duration.ofDays(7);

    private final ObjectMapper mapper = new ObjectMapper();

    /** Reads and parses the cart cookie; returns an empty map when absent/invalid. */
    public Map<Integer, Integer> read(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    try {
                        String json = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
                        return mapper.readValue(json, new TypeReference<LinkedHashMap<Integer, Integer>>() {});
                    } catch (Exception ignored) {
                        return new LinkedHashMap<>();
                    }
                }
            }
        }
        return new LinkedHashMap<>();
    }

    /** Serializes the cart back into the response cookie. */
    public void write(HttpServletResponse response, Map<Integer, Integer> items) {
        String value;
        try {
            value = URLEncoder.encode(mapper.writeValueAsString(items), StandardCharsets.UTF_8);
        } catch (Exception e) {
            value = "";
        }
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, value)
                .path("/")
                .httpOnly(true)
                .sameSite("Lax")
                .maxAge(MAX_AGE)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /** Expires the cart cookie. */
    public void clear(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, "")
                .path("/")
                .httpOnly(true)
                .sameSite("Lax")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
