package com.capstone.service;

import com.capstone.dto.request.PasswordSetupRequest;
import com.capstone.dto.request.UserRegistrationRequest;
import com.capstone.dto.response.*;
import org.springframework.data.domain.Pageable;

public interface RegistrationService {
    ApiResponseDto<UserRegistrationResponse> inviteUser(UserRegistrationRequest request);
    ApiResponseDto<PasswordSetupResponse> completeRegistration(String token,PasswordSetupRequest request);
    ApiResponseDto<String> resendInvitation(String email);
}
