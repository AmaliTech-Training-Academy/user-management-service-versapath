package com.capstone.service.emails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class EmailTemplateService {

    @Value("${REGISTRATION_EMAIL_BODY_FILE}")
    private String registrationEmailBodyFile;
    /**
     * Load and populate registration invitation template
     */
    public String buildRegistrationInvitationHtml(String registrationLink, String roleName, String roleDescription) {
        try {
            String template = loadTemplate();

            return template
                    .replace("{{ROLE_NAME}}", roleName)
                    .replace("{{ROLE_DESCRIPTION}}", roleDescription)
                    .replace("{{REGISTRATION_LINK}}", registrationLink);

        } catch (IOException e) {
            log.error("Failed to load registration invitation template", e);
            return getFallbackRegistrationTemplate(registrationLink, roleName);
        }
    }

    /**
     * Load template from resources
     */
    private String loadTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource(registrationEmailBodyFile);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    /**
     * Fallback template if file loading fails
     */
    private String getFallbackRegistrationTemplate(String registrationLink, String roleName) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2>Welcome to VersaPath</h2>
                <p>You have been invited to join as a %s.</p>
                <a href="%s" style="background: #00708A; color: white; padding: 10px 20px; text-decoration: none;">
                    Complete Registration
                </a>
                <p>Best regards,<br>The VersaPath Team</p>
            </body>
            </html>
            """, roleName, registrationLink);
    }
}
