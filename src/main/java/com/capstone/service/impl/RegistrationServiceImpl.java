package com.capstone.service.impl;

import com.capstone.dto.request.PasswordSetupRequest;
import com.capstone.dto.request.UserRegistrationRequest;
import com.capstone.dto.response.*;
import com.capstone.exception.*;
import com.capstone.mapper.RegistrationMapper;
import com.capstone.messaging.RabbitMQProducer;
import com.capstone.model.EStatus;
import com.capstone.model.Role;
import com.capstone.model.User;
import com.capstone.repository.RoleRepository;
import com.capstone.repository.UserRepository;
import com.capstone.service.EmailService;
import com.capstone.service.RegistrationService;
import com.capstone.util.RegistrationTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.producer.ProduceUserEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RegistrationServiceImpl implements RegistrationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final RegistrationTokenUtil tokenUtil;
    private final RegistrationMapper registrationMapper;
    private final PasswordEncoder passwordEncoder;
    private final RabbitMQProducer rabbitMQProducer;

    @Value("${REGISTRATION_EMAIL_FE_URI}")
    private String registrationEmailFeUri;

    @Override
    public ApiResponseDto<UserRegistrationResponse> inviteUser(UserRegistrationRequest request){
        log.info("Processing user invitation for email: {}", request.getEmail());

        try{
            // Validate that user doesn't already exist
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
            }

            // Validate that role exists
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RoleNotFoundException("Role not found with ID: " + request.getRoleId()));

            //Create user with PENDING status
            User user = registrationMapper.toEntity(request);
            user.setRole(role);
            user.setStatus(EStatus.PENDING);

            User savedUser = userRepository.save(user);
            log.info("Created pending user with ID: {} for email: {}", savedUser.getId(), savedUser.getEmail());

            //Generate registration token
            String token = tokenUtil.generateRegistrationToken(
                    savedUser.getId(),
                    savedUser.getEmail()
            );

            //Build registration link
            String registrationLink = String.format("%s?invite=%s&email=%s", registrationEmailFeUri, token, user.getEmail());

            //Send invitation email
            try {
                emailService.sendRegistrationInvitation(
                        savedUser.getEmail(),
                        registrationLink,
                        role.getRole()
                );
            } catch (Exception emailException) {
                log.error("Failed to send invitation email to: {}", savedUser.getEmail(), emailException);
                // Rollback user creation if email fails
                userRepository.delete(savedUser);
                throw new EmailSendingException("Failed to send invitation email");
            }

            // Build response
            UserRegistrationResponse response = registrationMapper.toRegistrationResponse(savedUser);
            response.setRegistrationLink(registrationLink);

            // Format expiration time
            Instant expirationTime = Instant.now().plusMillis(86400000); // 24 hours
            String formattedExpiration = expirationTime.atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            response.setTokenExpiresAt(formattedExpiration);

            log.info("Successfully sent invitation to: {}", request.getEmail());

            return ApiResponseDto.success(response, "User invitation sent successfully");

        } catch (UserAlreadyExistsException | RoleNotFoundException | EmailSendingException e) {
            throw e; // Re-throw known exceptions
        } catch (Exception e) {
            log.error("Unexpected error during user invitation for email: {}", request.getEmail(), e);
            throw new RuntimeException("Failed to process user invitation", e);
        }
    }

    @Override
    public ApiResponseDto<PasswordSetupResponse> completeRegistration(String token,PasswordSetupRequest request){
        log.info("Processing registration completion for token");

        try{
            // Validate and parse token
            RegistrationTokenUtil.RegistrationTokenData tokenData;
            try {
                tokenData = tokenUtil.validateAndParseToken(token);
            } catch (RuntimeException e) {
                throw new InvalidRegistrationTokenException("Invalid or expired registration token");
            }

            // Validate passwords match
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                throw new PasswordMismatchException("Password and confirmation password do not match");
            }

            // Find user and validate status
            User user = userRepository.findByIdWithRole(tokenData.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            if (user.getStatus() != EStatus.PENDING) {
                throw new UserNotPendingException("Invalid Request");
            }

            // Verify email matches
            if (!user.getEmail().equals(tokenData.getEmail())) {
                throw new InvalidRegistrationTokenException("Invalid Token");
            }

            // Check if username is already taken
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new UsernameAlreadyExistsException("Username '" + request.getUsername() + "' is already taken");
            }

            // pdate user with registration completion data
            registrationMapper.updateUserFromRegistrationCompletion(user, request);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setStatus(EStatus.ACTIVE);

            User updatedUser = userRepository.save(user);
            log.info("Successfully completed registration for user: {} with username: {}",
                    updatedUser.getId(), updatedUser.getUsername());

            try {
                ProduceUserEvent userEvent = ProduceUserEvent.builder()
                        .versapathUserId(updatedUser.getId())
                        .email(updatedUser.getEmail())
                        .firstName(updatedUser.getFirstName())
                        .lastName(updatedUser.getLastName())
                        .username(updatedUser.getUsername())
                        .build();

                rabbitMQProducer.sendUserEvent(userEvent);
                log.info("Successfully published user event for Moodle integration: {}", updatedUser.getUsername());
            } catch (Exception eventException) {
                log.error("Failed to publish user event for user: {}", updatedUser.getUsername(), eventException);
                throw new EventPublishingException("Failed to publish user event for Moodle integration", eventException);
            }

            // Build response
            PasswordSetupResponse response = registrationMapper.toPasswordSetupResponse(updatedUser);
            return ApiResponseDto.success(response, "Registration completed successfully. You can now log in with your credentials.");

        } catch (InvalidRegistrationTokenException | PasswordMismatchException |
                 UserNotFoundException | UserNotPendingException | UsernameAlreadyExistsException e) {
            throw e; // Re-throw known exceptions
        } catch (Exception e) {
            log.error("Unexpected error during registration completion", e);
            throw new RuntimeException("Failed to complete registration", e);
        }
    }

    @Override
    public ApiResponseDto<String> resendInvitation(String email) {
        log.info("Processing resend invitation for email: {}", email);

        try {
            // Find user by email with PENDING status
            User user = userRepository.findByEmailAndStatus(email, EStatus.PENDING)
                    .orElseThrow(() -> new UserNotFoundException("Invalid Request"));

            // Generate new registration token
            String token = tokenUtil.generateRegistrationToken(
                    user.getId(),
                    user.getEmail()
            );

            // Build registration link
            String registrationLink = String.format("%s?invite=%s&email=%s", registrationEmailFeUri, token, user.getEmail());

            // Resend invitation email
            try {
                emailService.sendRegistrationInvitation(
                        user.getEmail(),
                        registrationLink,
                        user.getRole().getRole()
                );
            } catch (Exception emailException) {
                log.error("Failed to resend invitation email to: {}", user.getEmail(), emailException);
                throw new EmailSendingException("Failed to resend invitation email");
            }

            log.info("Successfully resent invitation to: {}", email);

            return ApiResponseDto.success("Invitation email resent successfully");

        } catch (UserNotFoundException | EmailSendingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during invitation resend for email: {}", email, e);
            throw new RuntimeException("Failed to resend invitation", e);
        }
    }

}
