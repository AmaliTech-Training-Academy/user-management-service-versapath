package com.capstone.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${APP_BASE_URL:http://localhost:8090}")
    private String baseUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url(baseUrl).description("Local Development Server"),
                        new Server().url("https://api.versapath.ai").description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    private Info apiInfo() {
        return new Info()
                .title("VersaPath User Management API")
                .description("""
                    ## VersaPath User Management Service
                    
                    This service handles user registration, authentication, and user management for the VersaPath platform.
                    
                    ### Key Features:
                    - **User Registration**: Admin-initiated user invitations with secure token-based registration completion
                    - **Role Management**: Support for ADMIN, MANAGER, MENTOR, and LEARNER roles
                    - **Security**: JWT-based authentication with role-based access control
                    - **Email Notifications**: Automated invitation emails
                    
                    ### Getting Started:
                    1. Use the `/api/v1/auth/invite-user` endpoint to invite new users (Admin only)
                    2. Users complete registration via the link in their invitation email
                    3. Use appropriate endpoints based on your role and permissions
                    
                    ### Authentication:
                    - Most endpoints require authentication via JWT Bearer token
                    - Registration completion and token validation are public endpoints
                    - Admin-specific endpoints require ADMIN role
                    """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("VersaPath Development Team")
                        .email("dev@versapath.com")
                        .url("https://versapath.ai"));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("""
                    **JWT Bearer Token Authentication**
                    
                    Enter your JWT token in the format: `your-jwt-token-here`
                    
                    **How to get a token:**
                    1. Complete user registration via invitation email
                    2. Use the login endpoint (when implemented)
                    3. Use the token in the Authorization header as: `Bearer your-jwt-token-here`
                    
                    **Token expiration:** Tokens expire after 24 hours for security
                    """);
    }

    @Bean
    public io.swagger.v3.oas.models.tags.Tag userRegistrationTag() {
        return new io.swagger.v3.oas.models.tags.Tag()
                .name("User Registration")
                .description("Endpoints for user invitation, registration completion, and related operations");
    }

    @Bean
    public io.swagger.v3.oas.models.tags.Tag roleManagementTag() {
        return new io.swagger.v3.oas.models.tags.Tag()
                .name("Role Management")
                .description("Endpoints for managing user roles and permissions (Admin only)");
    }

    @Bean
    public io.swagger.v3.oas.models.tags.Tag userManagementTag() {
        return new io.swagger.v3.oas.models.tags.Tag()
                .name("User Management")
                .description("Endpoints for user profile management and user operations");
    }
}
