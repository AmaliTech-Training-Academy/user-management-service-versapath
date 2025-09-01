package com.capstone.service;

import com.capstone.dto.request.LoginRequestDto;
import com.capstone.dto.response.LoginResponseDto;
import com.capstone.dto.response.LogoutResponseDto;
import com.capstone.dto.response.RefreshTokenResponseDto;
import com.capstone.dto.response.UserProfileDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {
    LoginResponseDto login(LoginRequestDto loginRequest, HttpServletResponse response);
    RefreshTokenResponseDto refreshToken(HttpServletRequest request, HttpServletResponse response);
    LogoutResponseDto logout(HttpServletResponse response, HttpServletRequest request);
    UserProfileDto getCurrentUser();
}
