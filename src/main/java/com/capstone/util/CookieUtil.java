package com.capstone.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CookieUtil {
    @Value("${JWT_REFRESH_TOKEN_EXPIRATION_MS:604800}") // 7 days default
    private int refreshTokenExpiry;

    @Value("${APP_SECURITY_SECURE_COOKIES:true}")
    private boolean secureCookies;

    @Value("${APP_SECURITY_COOKIE_PATH:/api/v1/auth}")
    private String cookiePath;

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(secureCookies) // true in production
                .sameSite("Strict")
                .maxAge(refreshTokenExpiry)
                .path(cookiePath)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        log.debug("Refresh token cookie set successfully");
    }

    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite("Strict")
                .maxAge(0)
                .path(cookiePath)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        log.debug("Refresh token cookie cleared");
    }
}
