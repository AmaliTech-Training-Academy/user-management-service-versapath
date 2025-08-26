package com.capstone.service.impl;

import com.capstone.model.ERole;
import com.capstone.service.EmailService;
import com.capstone.service.emails.EmailTemplateService;
import com.capstone.service.emails.RoleDescriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateService templateService;
    private final RoleDescriptionService roleDescriptorService;

    @Value("${APP_NOTIFICATION_EMAIL_FROM}")
    private String fromEmail;

    @Value("${APP_NOTIFICATION_EMAIL_SUBJECT_PREFIX}")
    private String subjectPrefix;

    /**
     * Send registration invitation email with role-specific content
     */
    public void sendRegistrationInvitation(String toEmail, String registrationLink, ERole role) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);

            String roleName = roleDescriptorService.getRoleName(role);
            helper.setSubject(String.format("%s Welcome to VersaPath - %s Invitation", subjectPrefix, roleName));

            String roleDescription = roleDescriptorService.getRoleDescription(role);
            String htmlBody = templateService.buildRegistrationInvitationHtml(registrationLink, roleName, roleDescription);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Registration invitation email sent successfully to: {} for role: {}", toEmail, role);

        } catch (MessagingException e) {
            log.error("Failed to send registration invitation email to: {} for role: {}", toEmail, role, e);
            throw new RuntimeException("Failed to send registration invitation email", e);
        }
    }
}
