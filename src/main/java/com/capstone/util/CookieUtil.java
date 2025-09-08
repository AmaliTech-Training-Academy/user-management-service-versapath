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

    @Value("${JWT_ACCESS_TOKEN_EXPIRATION_MS:900000}") // 15 minutes
    private int accessTokenExpiry;

    @Value("${JWT_REFRESH_TOKEN_EXPIRATION_MS:604800}") // 7 days default
    private int refreshTokenExpiry;

    @Value("${APP_SECURITY_SECURE_COOKIES:true}")
    private boolean secureCookies;

    @Value("${APP_SECURITY_COOKIE_REFRESH_TOKEN_PATH:/api/v1/auth}")
    private String refreshTokenCookiePath;

    @Value("${APP_SECURITY_COOKIE_ACCESS_TOKEN_PATH:/api/v1/auth}")
    private String accessTokenCookiePath;

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    // Access Token Cookie Methods
    public void addAccessTokenCookie(HttpServletResponse response, String accessToken) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, accessToken)
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite("Strict")
                .maxAge(accessTokenExpiry / 1000)
                .path(accessTokenCookiePath)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        log.debug("Access token cookie set successfully");
    }

    public String getAccessTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public void clearAccessTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite("Strict")
                .maxAge(0)
                .path(accessTokenCookiePath)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        log.debug("Access token cookie cleared");
    }

    // Existing Refresh Token Cookie Methods (unchanged)
    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite("Strict")
                .maxAge(refreshTokenExpiry)
                .path(refreshTokenCookiePath)
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
                .path(refreshTokenCookiePath)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        log.debug("Refresh token cookie cleared");
    }

    // Add HTTP Response Headers Methods
    public void addTokensToHeaders(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addHeader("Authorization", "Bearer " + accessToken);
        response.addHeader("X-Refresh-Token", refreshToken);
        log.debug("Tokens added to HTTP response headers");
    }

    public void addAccessTokenToHeader(HttpServletResponse response, String accessToken) {
        response.addHeader("Authorization", "Bearer " + accessToken);
        log.debug("Access token added to Authorization header");
    }

    // Utility method to clear all auth cookies at once
    public void clearAllAuthCookies(HttpServletResponse response) {
        clearAccessTokenCookie(response);
        clearRefreshTokenCookie(response);
        log.debug("All authentication cookies cleared");
    }
}
