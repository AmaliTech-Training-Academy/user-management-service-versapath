package com.capstone.service;

public interface AuditService {

    void logUserRoleUpdate(String targetUserId, String oldRole, String newRole, String actorId, String actorName, String actorRole);
    void logUserStatusUpdate(String targetUserId, String oldStatus, String newStatus, String actorId, String actorName, String actorRole);
    void logUserDataAccess(String targetUserId, String action, String actorId, String actorName, String actorRole);
    void logBulkUserAccess(String action, String params, String actorId, String actorName, String actorRole);
    void logUserProfileUpdate(String userId, String userName);
    void logUserPasswordChange(String userId, String userName);
    void logSystemAction(String action, String targetUserId, String details);
}
