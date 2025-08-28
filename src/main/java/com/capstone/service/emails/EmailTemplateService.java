package com.capstone.service.emails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@Service
@Slf4j
@RequiredArgsConstructor
public class EmailTemplateService {

    private final TemplateEngine templateEngine;

    @Value("${REGISTRATION_EMAIL_BODY_FILE}")
    private String registrationEmailBodyFile;

    @Value("${PASSWORD_RESET_EMAIL_BODY_FILE}")
    private String passwordResetEmailBodyFile;

    @Value("${APP_BASE_FE_URL}")
    private String  baseFeUrl;

    public String buildRegistrationInvitationHtml(String registrationLink, String roleName) {
        Context context = new Context();
        context.setVariable("role", roleName);
        context.setVariable("invite", registrationLink);

        return templateEngine.process(registrationEmailBodyFile, context);
    }

    public String buildPasswordResetHtml(String resetLink, String roleName) {
        Context context = new Context();
        context.setVariable("roleName", roleName);
        context.setVariable("resetLink", resetLink);
        context.setVariable("loginLink", baseFeUrl);

        return templateEngine.process(passwordResetEmailBodyFile, context);
    }
}
