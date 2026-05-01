package com.projectmanager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded document representing a project member with their project-specific role.
 * This is embedded inside the Project document (not a separate collection).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMember {

    /** Reference to User.id */
    private String userId;

    /** Project-level role: ADMIN or MEMBER */
    private String role;

    /** Denormalized user name for display without extra DB lookups */
    private String userName;

    /** Denormalized user email for display */
    private String userEmail;
}
