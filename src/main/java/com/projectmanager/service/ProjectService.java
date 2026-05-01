package com.projectmanager.service;

import com.projectmanager.dto.project.AddMemberRequest;
import com.projectmanager.dto.project.ProjectRequest;
import com.projectmanager.dto.project.ProjectResponse;
import com.projectmanager.exception.BadRequestException;
import com.projectmanager.exception.ResourceNotFoundException;
import com.projectmanager.exception.UnauthorizedException;
import com.projectmanager.model.Project;
import com.projectmanager.model.ProjectMember;
import com.projectmanager.model.User;
import com.projectmanager.repository.ProjectRepository;
import com.projectmanager.repository.TaskRepository;
import com.projectmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Business logic for project management including CRUD and member management.
 * RBAC is enforced at this layer.
 */
@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    // ─────────────────────────────────── CRUD ────────────────────────────────────

    /**
     * Get all projects where the current user is a member.
     */
    public List<ProjectResponse> getProjectsForUser(String userId) {
        List<Project> projects = projectRepository.findByMembersUserId(userId);
        return projects.stream()
                .map(p -> toResponse(p, userId))
                .collect(Collectors.toList());
    }

    /**
     * Get a single project by ID. User must be a member.
     */
    public ProjectResponse getProjectById(String projectId, String userId) {
        Project project = findProjectOrThrow(projectId);
        assertMember(project, userId);
        return toResponse(project, userId);
    }

    /**
     * Create a new project. The creator is automatically added as ADMIN.
     */
    public ProjectResponse createProject(ProjectRequest request, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ProjectMember ownerMember = ProjectMember.builder()
                .userId(userId)
                .role("ADMIN")
                .userName(user.getName())
                .userEmail(user.getEmail())
                .build();

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .ownerId(userId)
                .members(new java.util.ArrayList<>(List.of(ownerMember)))
                .build();
        project.onCreate();

        Project saved = projectRepository.save(project);
        return toResponse(saved, userId);
    }

    /**
     * Update a project. Only project admins may do this.
     */
    public ProjectResponse updateProject(String projectId, ProjectRequest request, String userId) {
        Project project = findProjectOrThrow(projectId);
        assertAdmin(project, userId);

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.onUpdate();

        Project saved = projectRepository.save(project);
        return toResponse(saved, userId);
    }

    /**
     * Delete a project and all its tasks. Only project admins may do this.
     */
    public void deleteProject(String projectId, String userId) {
        Project project = findProjectOrThrow(projectId);
        assertAdmin(project, userId);

        // Cascade delete all tasks in this project
        List<com.projectmanager.model.Task> tasks = taskRepository.findByProjectId(projectId);
        taskRepository.deleteAll(tasks);

        projectRepository.delete(project);
    }

    // ─────────────────────────────── Member management ───────────────────────────

    /**
     * Add a member to a project by email. Only project admins may do this.
     */
    public ProjectResponse addMember(String projectId, AddMemberRequest request, String currentUserId) {
        Project project = findProjectOrThrow(projectId);
        assertAdmin(project, currentUserId);

        User userToAdd = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + request.getEmail()));

        // Check if already a member
        boolean alreadyMember = project.getMembers().stream()
                .anyMatch(m -> m.getUserId().equals(userToAdd.getId()));
        if (alreadyMember) {
            throw new BadRequestException("User is already a member of this project");
        }

        ProjectMember newMember = ProjectMember.builder()
                .userId(userToAdd.getId())
                .role(request.getRole())
                .userName(userToAdd.getName())
                .userEmail(userToAdd.getEmail())
                .build();

        project.getMembers().add(newMember);
        project.onUpdate();
        Project saved = projectRepository.save(project);
        return toResponse(saved, currentUserId);
    }

    /**
     * Remove a member from a project. Only project admins may do this.
     * The owner cannot be removed.
     */
    public ProjectResponse removeMember(String projectId, String userIdToRemove, String currentUserId) {
        Project project = findProjectOrThrow(projectId);
        assertAdmin(project, currentUserId);

        if (project.getOwnerId().equals(userIdToRemove)) {
            throw new BadRequestException("Cannot remove the project owner");
        }

        boolean removed = project.getMembers()
                .removeIf(m -> m.getUserId().equals(userIdToRemove));

        if (!removed) {
            throw new ResourceNotFoundException("User is not a member of this project");
        }

        project.onUpdate();
        Project saved = projectRepository.save(project);
        return toResponse(saved, currentUserId);
    }

    // ───────────────────────────────── Helpers ──────────────────────────────────

    public Project findProjectOrThrow(String projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id: " + projectId));
    }

    /** Get the project-level role of the user */
    public String getUserRoleInProject(Project project, String userId) {
        return project.getMembers().stream()
                .filter(m -> m.getUserId().equals(userId))
                .map(ProjectMember::getRole)
                .findFirst()
                .orElse(null);
    }

    public boolean isAdminInProject(Project project, String userId) {
        return "ADMIN".equals(getUserRoleInProject(project, userId));
    }

    public boolean isMemberOfProject(Project project, String userId) {
        return project.getMembers().stream()
                .anyMatch(m -> m.getUserId().equals(userId));
    }

    private void assertMember(Project project, String userId) {
        if (!isMemberOfProject(project, userId)) {
            throw new UnauthorizedException("You are not a member of this project");
        }
    }

    private void assertAdmin(Project project, String userId) {
        if (!isAdminInProject(project, userId)) {
            throw new UnauthorizedException("Only project admins can perform this action");
        }
    }

    private ProjectResponse toResponse(Project project, String userId) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerId(project.getOwnerId())
                .members(project.getMembers())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .currentUserRole(getUserRoleInProject(project, userId))
                .build();
    }
}
