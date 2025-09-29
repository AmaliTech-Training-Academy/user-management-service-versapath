package com.capstone.service;

import com.capstone.dto.request.LoginRequestDto;
import com.capstone.dto.response.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {
    LoginResponseDto login(LoginRequestDto loginRequest, HttpServletResponse response, HttpServletRequest request);
    RefreshTokenResponseDto refreshToken(HttpServletRequest request, HttpServletResponse response);
    LogoutResponseDto logout(HttpServletResponse response, HttpServletRequest request);
    UserProfileDto getCurrentUser();
    ApiResponseDto<String> completeOnboarding();
}
