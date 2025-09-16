package com.capstone.service.impl;

import com.capstone.service.AuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class AuditServiceImpl implements AuditService {

    private static final String AUDIT_PREFIX = "[AUDIT]";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void logUserRoleUpdate(String targetUserId, String oldRole, String newRole, String actorId, String actorName, String actorRole) {
        String logMessage = String.format("%s [%s] ROLE_UPDATE - user::%s, from::%s to::%s, done-by::%s(%s)::%s",
                AUDIT_PREFIX,
                LocalDateTime.now().format(TIMESTAMP_FORMAT),
                targetUserId,
                oldRole,
                newRole,
                actorId,
                actorName,
                actorRole
        );
        log.info(logMessage);
    }

    @Override
    public void logUserStatusUpdate(String targetUserId, String oldStatus, String newStatus, String actorId, String actorName, String
            actorRole) {
        String logMessage = String.format("%s [%s] STATUS_UPDATE - user::%s, from::%s to::%s, done-by::%s(%s)::%s",
                AUDIT_PREFIX,
                LocalDateTime.now().format(TIMESTAMP_FORMAT),
                targetUserId,
                oldStatus,
                newStatus,
                actorId,
                actorName,
                actorRole
        );
        log.info(logMessage);
    }

    @Override
    public void logUserDataAccess(String targetUserId, String action, String actorId, String actorName, String actorRole) {
        String logMessage = String.format("%s [%s] USER_DATA_ACCESS - user::%s, action::%s, done-by::%s(%s)::%s",
                AUDIT_PREFIX,
                LocalDateTime.now().format(TIMESTAMP_FORMAT),
                targetUserId,
                action,
                actorId,
                actorName,
                actorRole
        );
        log.info(logMessage);
    }

    @Override
    public void logBulkUserAccess(String action, String params, String actorId, String actorName, String actorRole) {
        String logMessage = String.format("%s [%s] BULK_USER_ACCESS - action::%s, params::%s, done-by::%s(%s)::%s",
                AUDIT_PREFIX,
                LocalDateTime.now().format(TIMESTAMP_FORMAT),
                action,
                params,
                actorId,
                actorName,
                actorRole
        );
        log.info(logMessage);
    }

    @Override
    public void logUserProfileUpdate(String userId, String userName) {
        String logMessage = String.format("%s [%s] PROFILE_UPDATE - user::%s, done-by::SELF(%s)",
                AUDIT_PREFIX,
                LocalDateTime.now().format(TIMESTAMP_FORMAT),
                userId,
                userName
        );
        log.info(logMessage);
    }

    @Override
    public void logUserPasswordChange(String userId, String userName) {
        String logMessage = String.format("%s [%s] PASSWORD_CHANGE - user::%s, done-by::SELF(%s)",
                AUDIT_PREFIX,
                LocalDateTime.now().format(TIMESTAMP_FORMAT),
                userId,
                userName
        );
        log.info(logMessage);
    }

    @Override
    public void logSystemAction(String action, String targetUserId, String details) {
        String logMessage = String.format("%s [%s] SYSTEM_ACTION - action::%s, user::%s, details::%s, done-by::SYSTEM",
                AUDIT_PREFIX,
                LocalDateTime.now().format(TIMESTAMP_FORMAT),
                action,
                targetUserId,
                details
        );
        log.info(logMessage);
    }
}
