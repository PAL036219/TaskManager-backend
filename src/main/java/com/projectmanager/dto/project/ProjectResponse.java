package com.projectmanager.dto.project;

import com.projectmanager.model.ProjectMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response body for a project, including its member list.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {

    private String id;
    private String name;
    private String description;
    private String ownerId;
    private List<ProjectMember> members;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** The current user's role in this project (ADMIN or MEMBER) */
    private String currentUserRole;
}
