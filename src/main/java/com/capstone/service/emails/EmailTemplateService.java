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

    public String buildRegistrationInvitationHtml(String registrationLink, String roleName) {
        Context context = new Context();
        context.setVariable("role", roleName);
        context.setVariable("invite", registrationLink);

        return templateEngine.process(registrationEmailBodyFile, context);
    }

    public String buildPasswordResetHtml(String resetLink, String userName) {
        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("resetLink", resetLink);

        return templateEngine.process(passwordResetEmailBodyFile, context);
    }
}
